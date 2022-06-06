package com.sdm.auth.controller;

import com.sdm.auth.service.FacebookAuthService;
import com.sdm.core.controller.DefaultController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/facebook")
public class FacebookController extends DefaultController {

    @Autowired
    FacebookAuthService facebookAuthService;

    @GetMapping("/deletion")
    public ResponseEntity<Void> dataDeletion(@RequestParam(value = "id", required = false) String confirmationCode) {
        log.info("Received data deletion by => " + confirmationCode);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/deletion", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> dataDeletionCallback(@RequestParam("signed_request") String signedRequest) {
        Map<String, String> data = facebookAuthService.facebookDataDeletion(signedRequest);
        return ResponseEntity.ok(data);
    }
}
