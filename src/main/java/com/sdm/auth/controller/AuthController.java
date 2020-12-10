package com.sdm.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.admin.model.User;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.request.*;
import com.sdm.auth.service.AuthMailService;
import com.sdm.auth.service.AuthService;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.security.AESManager;
import com.sdm.core.security.SecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

@Controller
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService service;

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private AESManager aesManager;

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthMailService mailService;

    @PostMapping("/test")
    public ResponseEntity<User> test() {
        User user = userRepository.findById(1).orElseThrow();
        user.setEmail("saw.yoetha@gmail.com");

        mailService.welcomeUser(user, "1234", "Hello");
        return ResponseEntity.ok(user);
    }

    @PostMapping("")
    public ResponseEntity<User> authWithEmail(@Valid @RequestBody AuthRequest request) {
        return service.authByPassword(request);
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerByUserAndPassword(@Valid @RequestBody RegistrationRequest request) {
        return service.registerByUserAndEmail(request);
    }

    @PostMapping("/facebook")
    public ResponseEntity<User> facebookAuth(@Valid @RequestBody OAuth2Request request) throws IOException {
        return service.facebookAuth(request);
    }

    @PostMapping("/google")
    public ResponseEntity<User> googleAuth(@Valid @RequestBody OAuth2Request request) throws IOException {
        return service.googleAuth(request);
    }

    @PostMapping("/forgetPassword")
    public ResponseEntity<MessageResponse> forgetPassword(@Valid @RequestBody ForgetPasswordRequest request) {
        return service.forgetPassword(request);
    }

    @GetMapping("/activate")
    public ResponseEntity<MessageResponse> activateUser(@DefaultValue("") @RequestParam("token") String activateToken) {
        try {
            activateToken = securityManager.base64Decode(activateToken);
            ActivateRequest activateRequest = jacksonObjectMapper.readValue(activateToken, ActivateRequest.class);
            return service.accountActivation(activateRequest);
        } catch (JsonProcessingException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<User> resetPasswordWithOtp(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            String activateToken = aesManager.decrypt(request.getOldPassword(), request.getUser());
            ActivateRequest activateRequest = jacksonObjectMapper.readValue(activateToken, ActivateRequest.class);
            return service.resetPasswordByOtp(request, activateRequest);
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }
}
