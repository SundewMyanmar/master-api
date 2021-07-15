package com.sdm.sms.service;

import com.sdm.core.exception.GeneralException;
import com.sdm.core.util.Globalizer;
import com.sdm.sms.model.ValidPhone;
import com.sdm.sms.model.request.telenor.MessageType;
import com.sdm.sms.respository.ValidPhoneRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.jni.Global;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

@Service
@Log4j2
public class PhoneVerificationService {
    @Autowired
    private ValidPhoneRepository repository;

    @Autowired
    private TelenorSmsService smsService;

    public ValidPhone checkPhone(String phoneNumber) {
        phoneNumber = Globalizer.cleanPhoneNo(phoneNumber);
        if (Globalizer.isNullOrEmpty(phoneNumber)) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, "Invalid Phone Number");
        }

        ValidPhone phone = repository.findById(phoneNumber)
                .orElse(new ValidPhone(phoneNumber));
        if (!phone.isVerified()) {
            String secret = Globalizer.generateToken("0123456789", 6);
            phone.setSecret(secret);
            try {
                smsService.sendMessage(
                        "Your verification code is " + secret,
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
            throw new GeneralException(HttpStatus.BAD_REQUEST, "Invalid Phone Number");
        }

        ValidPhone phone = repository.findById(phoneNumber)
                .orElseThrow(() -> new GeneralException(HttpStatus.BAD_REQUEST, "Invalid phone number"));

        if (Globalizer.isNullOrEmpty(secretToken) || !phone.getSecret().equalsIgnoreCase(secretToken)) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, "Invalid verification code.");
        }
        phone.setVerifiedAt(new Date());
        phone.setVerified(true);
        repository.save(phone);

        return phone;
    }
}
