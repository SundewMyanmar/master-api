package com.sdm.master.controller;

import com.sdm.core.controller.ReadWriteController;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.repository.DefaultRepository;
import com.sdm.core.security.SecurityManager;
import com.sdm.master.entity.UserEntity;
import com.sdm.master.repository.UserRepository;
import com.sdm.master.request.ChangePasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController extends ReadWriteController<UserEntity, Long> {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private SecurityManager securityManager;

    @Override
    protected DefaultRepository<UserEntity, Long> getRepository() {
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

    @Override
    public ResponseEntity create(@Valid @RequestBody UserEntity request) {
        //Check user by user name
        userRepository.findByUserNameOrEmail(request.getUserName(), request.getEmail())
                .ifPresent(user -> {
                    if (user.getEmail().equalsIgnoreCase(request.getEmail())) {
                        throw new GeneralException(HttpStatus.BAD_REQUEST, "Sorry! someone already registered with this email");
                    } else if (user.getUserName().equalsIgnoreCase(request.getUserName())) {
                        throw new GeneralException(HttpStatus.BAD_REQUEST, "Sorry! someone already registered with this username");
                    }
                });

        String password = securityManager.hashString(request.getPassword());
        request.setPassword(password);

        UserEntity entity=userRepository.save(request);

        return new ResponseEntity(entity, HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity update(@Valid @RequestBody UserEntity request, @PathVariable("id") Long id) {
        UserEntity dbEntity = this.getRepository().findById(id)
                .orElseThrow(() -> new GeneralException(HttpStatus.NO_CONTENT,
                        "There is no any data by : " + id.toString()));

        if (!id.equals(request.getId())) {
            throw new GeneralException(HttpStatus.CONFLICT,
                    "Path ID and body ID aren't match.");
        }

        request.setUserName(dbEntity.getUserName());
        request.setEmail(dbEntity.getEmail());
        request.setPassword(dbEntity.getPassword());
        request.setFacebookId(dbEntity.getFacebookId());

        UserEntity entity = getRepository().save(request);
        return ResponseEntity.ok(entity);
    }
}
