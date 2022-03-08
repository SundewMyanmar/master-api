package com.sdm.auth.controller;

import com.sdm.admin.model.User;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.request.AuthRequest;
import com.sdm.auth.model.request.ChangePasswordRequest;
import com.sdm.auth.model.request.TokenInfo;
import com.sdm.auth.repository.TokenRepository;
import com.sdm.auth.service.AuthService;
import com.sdm.auth.service.FacebookAuthService;
import com.sdm.auth.service.GoogleAuthService;
import com.sdm.auth.service.JwtService;
import com.sdm.core.controller.DefaultController;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.BarCodeManager;
import com.sdm.core.util.Globalizer;
import com.sdm.storage.model.File;
import com.sdm.storage.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/me")
public class ProfileController extends DefaultController {

    public enum LINK_TYPE {
        GOOGLE,
        FACEBOOK
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired
    private FacebookAuthService facebookAuthService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private BarCodeManager barCodeManager;

    @Autowired
    private FileService fileService;

    private User checkMe() {
        return userRepository.findById(getCurrentUser().getUserId())
                .orElseThrow(() -> new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-user-account")));
    }

    @GetMapping("")
    public ResponseEntity<User> getProfile() {
        User user = this.checkMe();
        return ResponseEntity.ok(user);
    }

    @PostMapping("")
    public ResponseEntity<User> updateProfile(@RequestBody User user) {
        User existUser = this.checkMe();

        existUser.setProfileImage(user.getProfileImage());
        existUser.setDisplayName(user.getDisplayName());
        existUser.setExtras(user.getExtras());
        existUser = userRepository.save(existUser);

        return ResponseEntity.ok(existUser);
    }


    @PostMapping(value = "/changeProfileImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> uploadFile(@RequestParam("profileImage") List<MultipartFile> files) {
        User existUser = this.checkMe();

        if (files.size() > 0) {
            File file = fileService.create(files.get(0), true, true, null);
            existUser.setProfileImage(file);
            userRepository.save(existUser);
        }
        return new ResponseEntity<User>(existUser, HttpStatus.OK);
    }

    @PostMapping("/changePassword")
    public ResponseEntity<User> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String phoneOrEmail = request.getUser();
        if (Globalizer.isPhoneNo(phoneOrEmail)) phoneOrEmail = Globalizer.cleanPhoneNo(phoneOrEmail);

        User user = userRepository.findFirstByPhoneNumberOrEmail(phoneOrEmail, phoneOrEmail).orElseThrow(
                () -> new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-user-account")));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new GeneralException(HttpStatus.BAD_REQUEST,
                    localeManager.getMessage("invalid-user-or-old-password"));
        }

        String newPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(newPassword);
        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<User> refreshToken(@Valid @RequestBody TokenInfo tokenInfo, HttpServletRequest servletRequest) {
        User existUser = this.checkMe();

        String currentToken = jwtService.createToken(existUser, tokenInfo, servletRequest);
        existUser.setCurrentToken(currentToken);

        return ResponseEntity.ok(existUser);
    }

    @DeleteMapping("/cleanToken")
    public ResponseEntity<MessageResponse> cleanToken() {
        AuthRequest request = new AuthRequest();
        request.setDeviceId(getCurrentUser().getDeviceId());
        request.setDeviceOS(getCurrentUser().getDeviceOs());
        tokenRepository.cleanTokenByUserId(this.getCurrentUser().getUserId());
        MessageResponse message = new MessageResponse(HttpStatus.OK, localeManager.getMessage("remove-success"),
                localeManager.getMessage("clear-all-auth-token", this.getCurrentUser().getUserId()), null);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/linkOAuth2/{type}")
    public ResponseEntity<User> linkOAuth2(@PathVariable(value = "type") LINK_TYPE type,
                                           @RequestParam(value = "accessId", defaultValue = "") String accessId) {
        User user = this.checkMe();

        if (type.equals(LINK_TYPE.GOOGLE)) {
            return googleAuthService.link(accessId, user);
        } else if (type.equals(LINK_TYPE.FACEBOOK)) {
            return facebookAuthService.link(accessId, user);
        }

        throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-auth-linked-type"));
    }
}
