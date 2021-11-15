package com.sdm.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.LocaleManager;
import com.sdm.payment.exception.CallbackException;
import com.sdm.payment.exception.FailedType;
import com.sdm.payment.model.request.mpu.MPUPaymentInquiryResponseRequest;
import com.sdm.payment.util.PaymentSecurityManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log4j2
@Service
public class MPUPaymentService extends BasePaymentService {
    @Override
    public String getPayment() {
        return "MPU";
    }

    @Autowired
    private PaymentSecurityManager securityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LocaleManager localeManager;

    public MessageResponse paymentCallback(MPUPaymentInquiryResponseRequest request) throws CallbackException {
        try {
            writeLog(LogType.CALLBACK_REQUEST, objectMapper.writeValueAsString(request));
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
                    writeLog(LogType.ERROR, ex.getLocalizedMessage());
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
                throw new CallbackException(FailedType.INVALID_HASH, localeManager.getMessage("invalid-hash-value", getPayment()));
            }

            if (!request.getRespCode().equals("00")) {
                throw new CallbackException(FailedType.TRANSACTION_FAILED, localeManager.getMessage("transaction-failed", getPayment()));
            }
            return new MessageResponse(localeManager.getMessage("success"), localeManager.getMessage("mpu-callback-success", request.getInvoiceNo()));
        } catch (JsonProcessingException ex) {
            throw new CallbackException(FailedType.RUNTIME_ERROR, ex.getLocalizedMessage(), ex);
        }
    }

}
