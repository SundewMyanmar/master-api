package com.sdm.telenor.service;

import com.sdm.core.exception.GeneralException;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.LocaleManager;
import com.sdm.telenor.model.ValidPhone;
import com.sdm.telenor.model.request.telenor.MessageType;
import com.sdm.telenor.respository.ValidPhoneRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class PhoneVerificationService {
    @Autowired
    private ValidPhoneRepository repository;

    @Autowired
    private TelenorSmsService smsService;

    @Autowired
    private LocaleManager localeManager;

    public ValidPhone checkPhone(String phoneNumber) {
        log.info("Check Phone => ", phoneNumber);
        phoneNumber = Globalizer.cleanPhoneNo(phoneNumber);
        if (Globalizer.isNullOrEmpty(phoneNumber)) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-phone-number"));
        }

        ValidPhone phone = repository.findById(phoneNumber)
                .orElse(new ValidPhone(phoneNumber));
        if (!phone.isVerified()) {
            String secret = Globalizer.generateToken("0123456789", 6);
            phone.setSecret(secret);
            try {
                log.info("Send Verifing PIN => ", phoneNumber);
                smsService.sendMessage(
                        localeManager.getMessage("send-verification-code", secret),
                        new String[]{phoneNumber}, MessageType.MULTILINGUAL);
            } catch (IOException ex) {
                throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
            }
        }
        repository.save(phone);
        return phone;
    }

    public ValidPhone verifyPhone(String phoneNumber, String secretToken) {
        phoneNumber = Globalizer.cleanPhoneNo(phoneNumber);
        if (Globalizer.isNullOrEmpty(phoneNumber)) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-phone-number"));
        }

        ValidPhone phone = repository.findById(phoneNumber)
                .orElseThrow(() -> new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-phone-number")));

        if (Globalizer.isNullOrEmpty(secretToken) || !phone.getSecret().equalsIgnoreCase(secretToken)) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-phone-number"));
        }
        phone.setVerifiedAt(new Date());
        phone.setVerified(true);
        repository.save(phone);

        return phone;
    }
}
