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
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.AuthInfo;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.security.SecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/me")
public class ProfileController {

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

    private AuthInfo getCurrentUser() {
        return (AuthInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

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
}
