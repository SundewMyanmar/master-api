package com.sdm.master.controller;

import com.sdm.core.exception.GeneralException;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.security.model.AuthInfo;
import com.sdm.master.entity.UserEntity;
import com.sdm.master.repository.UserRepository;
import com.sdm.master.request.AuthRequest;
import com.sdm.master.request.ChangePasswordRequest;
import com.sdm.master.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/me")
public class ProfileController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityManager securityManager;

    private AuthInfo getCurrentUser() {
        return (AuthInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    //@UserAllowed
    @GetMapping("/")
    public ResponseEntity getProfile() {
        UserEntity user = userRepository.findById(getCurrentUser().getUserId())
            .orElseThrow(() -> new GeneralException(HttpStatus.NO_CONTENT, "Sorry! can't find your account."));
        return ResponseEntity.ok(user);
    }

    //@UserAllowed
    @PostMapping("/")
    public ResponseEntity getProfile(@Valid UserEntity user) {
        UserEntity existUser = userRepository.findById(getCurrentUser().getUserId())
            .orElseThrow(() -> new GeneralException(HttpStatus.NO_CONTENT, "Sorry! can't find your account."));

        user.setPassword(existUser.getPassword());
        user.setEmail(existUser.getEmail());
        user.setUsername(existUser.getUsername());
        user.setRoles(existUser.getRoles());
        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

    //@UserAllowed
    @PostMapping("/changePassword")
    public ResponseEntity getProfile(@Valid ChangePasswordRequest request) {
        UserEntity existUser = userRepository.findById(getCurrentUser().getUserId())
            .orElseThrow(() -> new GeneralException(HttpStatus.UNAUTHORIZED, "Sorry! you don't have permission."));

        String oldPassword = securityManager.hashString(request.getOldPassword());
        UserEntity authUser = userRepository.authByPassword(request.getUser(), oldPassword)
            .orElseThrow(() -> new GeneralException(HttpStatus.UNAUTHORIZED, "Sorry! you old password are not correct."));

        if (authUser.getId() != getCurrentUser().getUserId()) {
            throw new GeneralException(HttpStatus.UNAUTHORIZED,
                "There is no user (or) old password is wrong. Pls try again.");
        }

        String newPassword = securityManager.hashString(request.getNewPassword());
        authUser.setPassword(newPassword);
        userRepository.save(authUser);

        return ResponseEntity.ok(authUser);
    }

    //@UserAllowed
    @PostMapping("/clean")
    public ResponseEntity cleanToken(AuthRequest request) {
        return authService.authByPassword(request, true);
    }
}
