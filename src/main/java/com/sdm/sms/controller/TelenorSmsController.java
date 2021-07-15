package com.sdm.sms.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sdm.core.controller.DefaultController;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.util.Globalizer;
import com.sdm.sms.model.ValidPhone;
import com.sdm.sms.model.request.telenor.MessageType;
import com.sdm.sms.model.request.telenor.TelenorTokenSetting;
import com.sdm.sms.service.PhoneVerificationService;
import com.sdm.sms.service.TelenorSmsService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.*;

@Log4j2
@Controller
@RequestMapping("/public/sms")
public class TelenorSmsController extends DefaultController {
    @Autowired
    private TelenorSmsService telenorSmsService;

    @Autowired
    private PhoneVerificationService phoneVerificationService;

    @GetMapping("/telenor/callback")
    public ResponseEntity<TelenorTokenSetting> callback(@DefaultValue("") @RequestParam("code") String code,
                                                        @DefaultValue("") @RequestParam("scope") String scope) {
        try {
            log.info("TELENOR_CALLBACK => " + code + ", " + scope);
            TelenorTokenSetting result = this.telenorSmsService.requestAccessToken(code);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        }
    }


    @GetMapping("/verifyPhone/{phone}")
    private ResponseEntity<ValidPhone> verifyOrRegisterPhoneNumber(@PathVariable("phone") String phoneNumber,
                                                                   @DefaultValue("") @RequestParam(required = false, value = "secret") String secret) {
        if (Globalizer.isNullOrEmpty(secret)) {
            return ResponseEntity.ok(phoneVerificationService.checkPhone(phoneNumber));
        }

        ValidPhone phone = phoneVerificationService.verifyPhone(phoneNumber, secret);
        if (!phone.isVerified()) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, "Invalid verification code!");
        }

        return ResponseEntity.ok(phone);
    }
}
