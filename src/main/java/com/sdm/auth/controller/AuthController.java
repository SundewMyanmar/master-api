package com.sdm.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.admin.model.User;
import com.sdm.auth.model.request.*;
import com.sdm.auth.service.AuthService;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.util.security.AESManager;
import com.sdm.core.util.security.SecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

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

    @PostMapping("")
    public ResponseEntity<User> authWithEmail(@Valid @RequestBody AuthRequest request) {
        return service.authByPassword(request);
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerByUserAndPassword(@Valid @RequestBody RegistrationRequest request) {
        return service.registerByUserAndEmail(request);
    }

    @PostMapping("/anonymous")
    public ResponseEntity<User> anonymousAuth(@Valid @RequestBody AnonymousRequest request) {
        return service.anonymousAuth(request);
    }

    @PostMapping("/facebook")
    public ResponseEntity<User> facebookAuth(@Valid @RequestBody FacebookAuthRequest request) {
        return service.facebookAuth(request);
    }

    @PostMapping("/forgetPassword")
    public ResponseEntity<MessageResponse> forgetPassword(@Valid @RequestBody ForgetPassword request) {
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
