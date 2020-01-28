package com.sdm.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.auth.model.request.*;
import com.sdm.auth.service.AuthService;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.util.security.SecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService service;

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @PostMapping("")
    public ResponseEntity authWithEmail(@Valid @RequestBody AuthRequest request,
                                        @RequestHeader(HttpHeaders.USER_AGENT) String userAgent) {
        return service.authByPassword(request, userAgent);
    }

    @PostMapping("/register")
    public ResponseEntity registerByUserAndPassword(@Valid @RequestBody RegistrationRequest request,
                                                    @RequestHeader(HttpHeaders.USER_AGENT) String userAgent) {
        return service.registerByUserAndEmail(request, userAgent);
    }

    @PostMapping("/anonymous")
    public ResponseEntity anonymousAuth(@Valid @RequestBody AnonymousRequest request,
                                        @RequestHeader(HttpHeaders.USER_AGENT) String userAgent) {
        return service.anonymousAuth(request, userAgent);
    }

    @PostMapping("/facebook")
    public ResponseEntity facebookAuth(@Valid @RequestBody FacebookAuthRequest request,
                                       @RequestHeader(HttpHeaders.USER_AGENT) String userAgent) {
        return service.facebookAuth(request, userAgent);
    }

    @GetMapping("/forgetPassword")
    public ResponseEntity forgetPassword(@Valid @RequestParam("user") String user) {
        return service.forgetPassword(user);
    }

    @PostMapping("/activate")
    public ResponseEntity postActivate(@Valid ActivateRequest request) {
        return service.otpActivation(request);
    }

    @GetMapping("/activate")
    public ResponseEntity<String> getActivate(@DefaultValue("") @RequestParam("token") String activateToken) {
        try {
            activateToken = securityManager.base64Decode(activateToken);
            ActivateRequest activateRequest = jacksonObjectMapper.readValue(activateToken, ActivateRequest.class);
            return service.otpActivation(activateRequest);
        } catch (JsonProcessingException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<String> resetPasswordWithOtp(@Valid @RequestBody ChangePasswordRequest request,
                                                       @DefaultValue("") @RequestParam("token") String activateToken) {
        try {
            activateToken = securityManager.base64Decode(activateToken);
            ActivateRequest activateRequest = jacksonObjectMapper.readValue(activateToken, ActivateRequest.class);
            return service.resetPasswordWithOtp(request, activateRequest);
        } catch (JsonProcessingException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }
}
