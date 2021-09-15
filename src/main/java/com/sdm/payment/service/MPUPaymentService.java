package com.sdm.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.LocaleManager;
import com.sdm.payment.model.request.mpu.MPUPaymentInquiryResponseRequest;
import com.sdm.payment.util.PaymentSecurityManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log4j2
@Service
public class MPUPaymentService {
    @Autowired
    private PaymentSecurityManager securityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LocaleManager localeManager;

    public ResponseEntity<?> paymentRequestCallback(MPUPaymentInquiryResponseRequest request) {
        List<String> hashBuilder = new ArrayList<>();
        for (Field field : request.getClass().getDeclaredFields()) {
            if (field.getName().equals("hashValue") || field.getName().equals("serialVersionUID")) {
                continue;
            }
            field.setAccessible(true);

            try {
                Object value = field.get(request);
                if (!Globalizer.isNullOrEmpty(value)) {
                    hashBuilder.add(value.toString());
                }
            } catch (IllegalAccessException ex) {
                log.warn(ex.getLocalizedMessage());
            }
        }

        String[] values = new String[hashBuilder.size()];
        Arrays.sort(hashBuilder.toArray(values));

        String hashString = "";
        for (String value : values) {
            hashString += value;
        }
        String hash = securityManager.generateMPUHashHmac(hashString);
        if (!hash.equals(request.getHashValue())) {
            writeLog(request);

            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-payment-server-response"));
        }

        if (!request.getRespCode().equals("00")) {
            writeLog(request);
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("unprocessable-payment-response"));
        }
        return ResponseEntity.ok(new MessageResponse(localeManager.getMessage("success"), localeManager.getMessage("mpu-callback-success", request.getInvoiceNo())));
    }


    private void writeLog(MPUPaymentInquiryResponseRequest request) {
        try {
            log.error("INVALID_MPU_RESPONSE >>>" + objectMapper.writeValueAsString(request));
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("unprocessable-payment-response"));
        }
    }
}
