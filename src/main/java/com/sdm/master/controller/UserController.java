package com.sdm.master.controller;

import com.sdm.core.controller.ReadController;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.security.SecurityManager;
import com.sdm.master.entity.UserEntity;
import com.sdm.master.repository.UserRepository;
import com.sdm.master.request.ChangePasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController extends ReadController<UserEntity, Long> {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private SecurityManager securityManager;

    @Override
    protected JpaRepository<UserEntity, Long> getRepository() {
        return this.userRepository;
    }

    @PostMapping("/resetPassword/{userId}")
    public ResponseEntity getProfile(@PathVariable("userId") long userId,
                                     @Valid @RequestBody ChangePasswordRequest request) {
        UserEntity existUser = userRepository.findById(userId)
            .orElseThrow(() -> new GeneralException(HttpStatus.NO_CONTENT, "There is no user for your request!"));

        String newPassword = securityManager.hashString(request.getNewPassword());
        existUser.setPassword(newPassword);
        userRepository.save(existUser);

        return ResponseEntity.ok(existUser);
    }
}
