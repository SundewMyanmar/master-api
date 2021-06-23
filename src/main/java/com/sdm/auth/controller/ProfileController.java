package com.sdm.auth.controller;

import com.google.zxing.WriterException;
import com.sdm.admin.model.User;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.request.AuthRequest;
import com.sdm.auth.model.request.ChangePasswordRequest;
import com.sdm.auth.model.request.TokenInfo;
import com.sdm.auth.repository.TokenRepository;
import com.sdm.auth.service.*;
import com.sdm.core.controller.DefaultController;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.BarCodeManager;
import com.sdm.core.util.Globalizer;
import com.sdm.file.model.File;
import com.sdm.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
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

    @GetMapping("")
    public ResponseEntity<User> getProfile() {
        User user = userRepository.findById(getCurrentUser().getUserId())
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Sorry! can't find your account."));
        return ResponseEntity.ok(user);
    }

    @PostMapping("")
    public ResponseEntity<User> updateProfile(@RequestBody User user) {
        User existUser = userRepository.findById(getCurrentUser().getUserId())
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Sorry! can't find your account."));

        existUser.setProfileImage(user.getProfileImage());
        existUser.setMMDisplayName(user.getDisplayName());
        existUser.setExtras(user.getExtras());
        existUser = userRepository.save(existUser);

        return ResponseEntity.ok(existUser);
    }


    @PostMapping(value = "/changeProfileImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> uploadFile(@RequestParam("profileImage") List<MultipartFile> files) {
        User existUser = userRepository.findById(getCurrentUser().getUserId())
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Sorry! can't find your account."));

        if (files.size() > 0) {
            File file = fileService.create(files.get(0), true, false);
            existUser.setProfileImage(file);
            userRepository.save(existUser);
        }
        return new ResponseEntity<User>(existUser, HttpStatus.OK);
    }

    @PostMapping("/changePassword")
    public ResponseEntity<User> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String oldPassword = securityManager.hashString(request.getOldPassword());
        User authUser = userRepository.authByPassword(request.getUser(), oldPassword).orElseThrow(
                () -> new GeneralException(HttpStatus.UNAUTHORIZED, "Sorry! you old password are not correct."));

        if (!authUser.getId().equals(getCurrentUser().getUserId())) {
            throw new GeneralException(HttpStatus.UNAUTHORIZED,
                    "There is no user (or) old password is wrong. Pls try again.");
        }

        String newPassword = securityManager.hashString(request.getNewPassword());
        authUser.setPassword(newPassword);
        userRepository.save(authUser);

        return ResponseEntity.ok(authUser);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<User> refreshToken(@Valid @RequestBody TokenInfo tokenInfo, HttpServletRequest servletRequest) {
        User existUser = userRepository.findById(getCurrentUser().getUserId())
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Sorry! can't find your account."));

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
        MessageResponse message = new MessageResponse(HttpStatus.OK, "Successfully deleted.",
                "Cleaned all token by User ID : " + this.getCurrentUser().getUserId(), null);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/linkOAuth2/{type}")
    public ResponseEntity<User> linkOAuth2(@PathVariable(value = "type") LINK_TYPE type,
                                           @RequestParam(value = "accessId", defaultValue = "") String accessId) {
        User user = userRepository.findById(getCurrentUser().getUserId())
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Sorry! can't find your account."));

        if (type.equals(LINK_TYPE.GOOGLE)) {
            return googleAuthService.link(accessId, user);
        } else if (type.equals(LINK_TYPE.FACEBOOK)) {
            return facebookAuthService.link(accessId, user);
        }

        throw new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Invalid Type!");
    }

    @GetMapping("/enableMfa/{enable}")
    @Transactional
    public ResponseEntity<User> enableMfa(@PathVariable(value = "enable") Boolean enable, @RequestParam(value = "type") MfaService.TotpType type) throws GeneralSecurityException {
        User user = userRepository.findById(getCurrentUser().getUserId())
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Sorry! can't find your account."));

        if (enable) {
            if (Globalizer.isNullOrEmpty(user.getMfaSecret()))
                user.setMfaSecret(securityManager.generateBase32Secret());
            if (Globalizer.isNullOrEmpty(type)) {
                throw new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Invalid MFA type.");
            }
            user.setMfaType(type);
            user = userRepository.save(user);

            authService.enableMFA(user);
        } else {
            user.setMfaEnabled(false);
            user.setMfaType(null);
            user = userRepository.save(user);
        }

        return ResponseEntity.ok(user);
    }

    @GetMapping("/verifyMfa/{totp}")
    public ResponseEntity<User> verifyMfa(@PathVariable(value = "totp") String totp) throws GeneralSecurityException {
        User user = userRepository.findById(getCurrentUser().getUserId())
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Sorry! can't find your account."));
        authService.verifyMFA(user, totp);

        user.setMfaEnabled(true);
        user = userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/mfa/qr")
    public ResponseEntity<?> generateMfaQR(
            @RequestParam(value = "name", required = false, defaultValue = "128") String name,
            @RequestParam(value = "width", required = false, defaultValue = "128") int width,
            @RequestParam(value = "noMargin", required = false, defaultValue = "false") boolean noMargin) throws IOException, WriterException {
        User user = userRepository.findById(getCurrentUser().getUserId())
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Sorry! can't find your account."));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        if (Globalizer.isNullOrEmpty(name))
            name = "SUNDEW MYANMAR";
        String mfaData = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                name, user.getDisplayName(), user.getMfaSecret(), name);
        barCodeManager.createQR(outputStream, mfaData, width, noMargin);

        String filename = name + ".png";
        String attachment = "attachment; filename=\"" + filename + "\"";
        Resource resource = new ByteArrayResource(outputStream.toByteArray());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(MediaType.IMAGE_PNG_VALUE))
                .header(HttpHeaders.CONTENT_DISPOSITION, attachment)
                .body(resource);
    }
}
