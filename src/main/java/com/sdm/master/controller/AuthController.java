package com.sdm.master.controller;

import com.sdm.master.request.AuthRequest;
import com.sdm.master.request.RegistrationRequest;
import com.sdm.master.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService service;

    @PostMapping("/")
    public ResponseEntity authWithEmail(@Valid @RequestBody AuthRequest request,
                                        @RequestHeader(HttpHeaders.USER_AGENT) String userAgent) {
        return service.authByPassword(request, userAgent, false);
    }

    @PostMapping("/register")
    public ResponseEntity registerByUserAndPassword(@Valid @RequestBody RegistrationRequest request,
                                                    @RequestHeader(HttpHeaders.USER_AGENT) String userAgent) {
        return service.registerByUserAndEmail(request, userAgent);
    }

    /*
    @PostMapping({"/anonymous"})
    public ResponseEntity anonymousAuth(@Valid @RequestBody AnonymousRequest request,
                                        @RequestHeader(HttpHeaders.USER_AGENT) String userAgent) {
        request.setUserAgent(userAgent);
        return service.anonymousAuth(request);
    }
    */
}
