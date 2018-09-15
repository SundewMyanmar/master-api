package com.sdm.master.controller;

import com.sdm.master.request.AuthRequest;
import com.sdm.master.request.RegistrationRequest;
import com.sdm.master.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService service;

    @PostMapping("/")
    public ResponseEntity authWithEmail(@Valid @RequestBody AuthRequest request) {
        return service.authByPassword(request, true);
    }

    @PostMapping("/register")
    public ResponseEntity registerByUserAndPassword(@Valid @RequestBody RegistrationRequest request) {
        return service.registerByUserAndEmail(request);
    }

    @PostMapping({"/anonymous"})
    public ResponseEntity anonymousAuth(@Valid @RequestBody RegistrationRequest request) {
        return service.registerByUserAndEmail(request);
    }
}
