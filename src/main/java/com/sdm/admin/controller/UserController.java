package com.sdm.admin.controller;

import com.sdm.admin.model.User;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.request.ChangePasswordRequest;
import com.sdm.auth.repository.TokenRepository;
import com.sdm.auth.service.AuthMailService;
import com.sdm.core.controller.DefaultReadWriterController;
import com.sdm.core.db.DefaultRepository;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.util.security.SecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin/users")
public class UserController extends DefaultReadWriterController<User, Integer> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private AuthMailService mailService;

    @Autowired
    private SecurityManager securityManager;


    @Override
    protected DefaultRepository<User, Integer> getRepository() {
        return this.userRepository;
    }

    @PostMapping("/resetPassword/{userId}")
    public ResponseEntity<User> resetPassword(@PathVariable("userId") int userId,
                                              @Valid @RequestBody ChangePasswordRequest request) {
        User existUser = this.checkData(userId);
        String newPassword = securityManager.hashString(request.getNewPassword());
        existUser.setPassword(newPassword);
        userRepository.save(existUser);

        return ResponseEntity.ok(existUser);
    }

    @DeleteMapping("/cleanToken/{userId}")
    public ResponseEntity<MessageResponse> cleanToken(@PathVariable("userId") int userId) {
        User existUser = this.checkData(userId);
        tokenRepository.cleanTokenByUserId(existUser.getId());
        MessageResponse message = new MessageResponse("success", "Cleaned all token by User ID : " + this.getCurrentUser().getUserId());
        return ResponseEntity.ok(message);
    }

    @Override
    public ResponseEntity<User> create(@Valid User request) {
        //Check user by user name
        userRepository.findByPhoneNumberOrEmail(request.getPhoneNumber(), request.getEmail())
                .ifPresent(user -> {
                    if (user.getEmail().equalsIgnoreCase(request.getEmail())) {
                        throw new GeneralException(HttpStatus.BAD_REQUEST, "Sorry! someone already registered with this email");
                    } else if (user.getPhoneNumber().equalsIgnoreCase(request.getPhoneNumber())) {
                        throw new GeneralException(HttpStatus.BAD_REQUEST, "Sorry! someone already registered with this phone number.");
                    }
                });

        String password = securityManager.hashString(request.getPassword());
        request.setPassword(password);

        User entity = userRepository.save(request);
        mailService.welcomeUser(entity, request.getPassword(), "Welcome!");

        return new ResponseEntity<>(entity, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<User> update(@Valid User body, Integer id) {
        User dbEntity = this.getRepository().findById(id)
                .orElseThrow(() -> new GeneralException(HttpStatus.NO_CONTENT,
                        "There is no any data by : " + Integer.toString(id)));

        if (id != body.getId()) {
            throw new GeneralException(HttpStatus.CONFLICT,
                    "Path ID and body ID aren't match.");
        }

        body.setPhoneNumber(dbEntity.getPhoneNumber());
        body.setEmail(dbEntity.getEmail());
        body.setPassword(dbEntity.getPassword());
        body.setFacebookId(dbEntity.getFacebookId());

        User entity = getRepository().save(body);
        return ResponseEntity.ok(entity);
    }
}
