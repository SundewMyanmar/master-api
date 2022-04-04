package com.sdm.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.service.ISettingManager;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.LocaleManager;
import com.sdm.payment.config.properties.MPUProperties;
import com.sdm.payment.exception.CallbackException;
import com.sdm.payment.exception.FailedType;
import com.sdm.payment.model.request.mpu.HttpParameter;
import com.sdm.payment.model.request.mpu.MPUPayment;
import com.sdm.payment.model.request.mpu.MPUPaymentInquiryResponseRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
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

    private static final String VIEW_TEMPLATE = "mpu/payment";

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LocaleManager localeManager;

    @Autowired
    private ISettingManager settingManager;

    private MPUProperties getProperties() {
        MPUProperties properties = new MPUProperties();
        try {
            properties = settingManager.loadSetting(MPUProperties.class);
        } catch (IOException | IllegalAccessException ex) {
            log.error(ex.getLocalizedMessage());
        }
        return properties;
    }

    public String generateHash(String stringData) {
        String encryptData = securityManager.generateHashHmac(stringData, this.getProperties().getSecretKey(), "HmacSHA1");
        return encryptData;
    }

    public String encryptCard(String cardInfo) {
        byte[] secret = this.getProperties().getSecretKey().getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(secret, "AES");
        return securityManager.aesEncrypt(cardInfo, secretKey, "AES/ECB/PKCS5Padding", null);
    }

    public ModelAndView buildModelAndView(MPUPayment request, String callbackUrl) {
        if (!Globalizer.isNullOrEmpty(callbackUrl)) {
            request.setFrontendURL(callbackUrl);
        } else {
            request.setFrontendURL(this.getProperties().getSuccessUrl());
        }
        request.setMerchantID(this.getProperties().getMerchantId());
        request.setBackendURL(this.getProperties().getPaymentCallbackUrl());

        ModelAndView modelAndView = new ModelAndView(VIEW_TEMPLATE);
        modelAndView.addObject("paymentUrl", this.getProperties().getPaymentRequestUrl());

        List<String> hashBuilder = new ArrayList<>();
        for (Field field : request.getClass().getDeclaredFields()) {
            if (field.getName().equals("hashValue") || !field.isAnnotationPresent(HttpParameter.class)) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(request);
                HttpParameter httpParameter = field.getAnnotation(HttpParameter.class);
                if (!Globalizer.isNullOrEmpty(value)) {
                    if (field.getName().equals("cardInfo")) {
                        value = this.encryptCard(value.toString());
                    }
                    modelAndView.addObject(httpParameter.value(), value.toString());
                    hashBuilder.add(value.toString());
                }

            } catch (IllegalAccessException ex) {
                log.warn(ex.getLocalizedMessage());
            }
        }

        //Sort Input Values
        String[] values = new String[hashBuilder.size()];
        Arrays.sort(hashBuilder.toArray(values));

        String hashString = "";
        for (String value : values) {
            hashString += value;
        }
        String hash = securityManager.generateHashHmac(hashString, this.getProperties().getSecretKey(), "HmacSHA1");
        ;
        modelAndView.addObject("hashValue", hash);
        return modelAndView;
    }

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
            String hash = generateHash(hashString);
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
