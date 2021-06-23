package com.sdm.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.Constants;
import com.sdm.admin.model.User;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.request.*;
import com.sdm.auth.service.AuthMailService;
import com.sdm.auth.service.AuthService;
import com.sdm.auth.service.FacebookAuthService;
import com.sdm.auth.service.GoogleAuthService;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.security.AESManager;
import com.sdm.core.security.SecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Controller
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService service;

    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired
    private FacebookAuthService facebookAuthService;

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

    @PostMapping("")
    public ResponseEntity<User> authWithEmail(@Valid @RequestBody AuthRequest request) throws GeneralSecurityException {
        return service.authByPassword(request);
    }

    @PostMapping("/verifyMfa")
    public ResponseEntity<User> authWithEmailAndMfa(@Valid @RequestBody AuthRequest request) throws GeneralSecurityException {
        return service.authByPasswordAndMfa(request);
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerByUserAndPassword(@Valid @RequestBody RegistrationRequest request) {
        return service.registerByUserAndEmail(request);
    }

    @PostMapping("/facebook")
    public ResponseEntity<User> facebookAuth(@Valid @RequestBody OAuth2Request request) throws IOException {
        return facebookAuthService.auth(request);
    }

    @PostMapping("/google")
    public ResponseEntity<User> googleAuth(@Valid @RequestBody OAuth2Request request) throws IOException {
        return googleAuthService.auth(request);
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

    @GetMapping(value = "/resetPassword", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView resetPassword(@RequestParam("token") String token,
                                      @RequestParam("user") String user) throws JsonProcessingException {
        String activateToken = aesManager.decrypt(token, securityManager.getProperties().getEncryptSalt());
        ActivateRequest activateRequest = jacksonObjectMapper.readValue(activateToken, ActivateRequest.class);
        service.resetPasswordMail(activateRequest);

        ModelAndView response = new ModelAndView("auth/reset-password");
        response.addObject("title", Constants.APP_NAME);
        response.addObject("email", user);
        return response;
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<User> resetPasswordWithOtp(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            String activateToken = aesManager.decrypt(request.getOldPassword(), securityManager.getProperties().getEncryptSalt());
            ActivateRequest activateRequest = jacksonObjectMapper.readValue(activateToken, ActivateRequest.class);
            return service.resetPasswordJson(request, activateRequest);
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }
}
