package com.sdm.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.admin.model.User;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.request.ActivateRequest;
import com.sdm.auth.model.request.AuthRequest;
import com.sdm.auth.model.request.ChangePasswordRequest;
import com.sdm.auth.model.request.ForgetPasswordRequest;
import com.sdm.auth.model.request.OAuth2Request;
import com.sdm.auth.model.request.RegistrationRequest;
import com.sdm.auth.service.AppleAuthService;
import com.sdm.auth.service.AuthMailService;
import com.sdm.auth.service.AuthService;
import com.sdm.auth.service.FacebookAuthService;
import com.sdm.auth.service.GoogleAuthService;
import com.sdm.core.Constants;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.LocaleManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService service;

    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired
    private FacebookAuthService facebookAuthService;

    @Autowired
    private AppleAuthService appleAuthService;

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthMailService mailService;

    @Autowired
    private LocaleManager localeManager;

    @PostMapping("")
    public ResponseEntity<User> authWithEmail(@Valid @RequestBody AuthRequest request) throws GeneralSecurityException, IOException {
        return service.authByPassword(request);
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerByUserAndPassword(@Valid @RequestBody RegistrationRequest request) {
        return service.userRegistration(request);
    }

    @PostMapping("/apple")
    public ResponseEntity<User> appleAuth(@Valid @RequestBody OAuth2Request request) throws IOException {
        return appleAuthService.auth(request);
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
            activateToken = securityManager.aesDecrypt(activateToken);
            ActivateRequest activateRequest = jacksonObjectMapper.readValue(activateToken, ActivateRequest.class);
            return service.accountActivation(activateRequest);
        } catch (JsonProcessingException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    @PostMapping(value = "/resetPassword/checkSMS", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> resetPasswordBySms(@Valid @RequestBody ActivateRequest request) {
        boolean isValid = service.checkSMSOTP(request);
        try {
            if (isValid) {
                String token = securityManager.aesEncrypt(jacksonObjectMapper.writeValueAsString(request));
                return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, localeManager.getMessage("success"), token, null));
            }
        } catch (JsonProcessingException ex) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
        }
        throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-otp-code"));
    }

    @GetMapping(value = "/resetPassword", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView resetPassword(@RequestParam("token") String token,
                                      @RequestParam("user") String user) throws JsonProcessingException {
        String activateToken = securityManager.aesDecrypt(token);
        ActivateRequest activateRequest = jacksonObjectMapper.readValue(activateToken, ActivateRequest.class);
        service.resetPasswordMail(activateRequest);

        ModelAndView response = new ModelAndView("auth/reset-password");
        response.addObject("title", Constants.APP_NAME);
        response.addObject("email", user);
        return response;
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<User> resetPasswordWithNewPassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            String activateToken = securityManager.aesDecrypt(request.getOldPassword());
            ActivateRequest activateRequest = jacksonObjectMapper.readValue(activateToken, ActivateRequest.class);
            return service.resetPasswordJson(request, activateRequest);
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }
}
