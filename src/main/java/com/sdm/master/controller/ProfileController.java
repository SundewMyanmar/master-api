package com.sdm.master.controller;

import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.AuthInfo;
import com.sdm.core.security.SecurityManager;
import com.sdm.master.entity.UserEntity;
import com.sdm.master.repository.UserRepository;
import com.sdm.master.request.ChangePasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/me")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityManager securityManager;

    private AuthInfo getCurrentUser() {
        return (AuthInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // @UserAllowed
    @GetMapping({ "/", "" })
    public ResponseEntity getProfile() {
        UserEntity user = userRepository.findById(getCurrentUser().getUserId())
                .orElseThrow(() -> new GeneralException(HttpStatus.NO_CONTENT, "Sorry! can't find your account."));
        return ResponseEntity.ok(user);
    }

    // @UserAllowed
    @PostMapping({ "/", "" })
    public ResponseEntity updateProfile(@RequestBody UserEntity user) {
        UserEntity existUser = userRepository.findById(getCurrentUser().getUserId())
                .orElseThrow(() -> new GeneralException(HttpStatus.NO_CONTENT, "Sorry! can't find your account."));

        existUser.setProfileImage(user.getProfileImage());
        existUser.setMMDisplayName(user.getDisplayName());
        existUser.setExtras(user.getExtras());
        existUser = userRepository.save(existUser);

        return ResponseEntity.ok(existUser);
    }

    // @UserAllowed
    @PostMapping("/changePassword")
    public ResponseEntity changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        UserEntity existUser = userRepository.findById(getCurrentUser().getUserId())
                .orElseThrow(() -> new GeneralException(HttpStatus.UNAUTHORIZED, "Sorry! you don't have permission."));

        String oldPassword = securityManager.hashString(request.getOldPassword());
        UserEntity authUser = userRepository.authByPassword(request.getUser(), oldPassword).orElseThrow(
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
}
