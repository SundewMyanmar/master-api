package com.sdm.payment.controller;

import com.sdm.core.controller.DefaultController;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.security.AESManager;
import com.sdm.core.util.Globalizer;
import com.sdm.payment.config.properties.MPUProperties;
import com.sdm.payment.model.request.mpu.HttpParameter;
import com.sdm.payment.model.request.mpu.MPUPayment;
import com.sdm.payment.repository.MPUPaymentRepository;
import com.sdm.payment.util.PaymentSecurityManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/public/payments/mpu")
@Log4j2
public class MPUPaymentController extends DefaultController {

    @Autowired
    private MPUProperties mpuProperties;

    @Autowired
    private MPUPaymentRepository repository;

    @Autowired
    private AESManager aesManager;

    @Autowired
    private PaymentSecurityManager securityManager;

    private ModelAndView buildModel(MPUPayment request) {
        ModelAndView modelAndView = new ModelAndView("mpu/payment");
        modelAndView.addObject("paymentUrl", mpuProperties.getPaymentRequestUrl());

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
                        value = aesManager.encrypt(value.toString(), mpuProperties.getSecretKey());
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
        String hash = securityManager.generateMPUHashHmac(hashString);
        modelAndView.addObject("hashValue", hash);
        return modelAndView;
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView paymentRequest(@PathVariable("id") String id,
                                       @DefaultValue("") @RequestParam(value = "callback", required = false) String callbackUrl) {
        MPUPayment request = repository.findById(id)
                .orElseThrow(() -> new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("no-data")));
        if (!Globalizer.isNullOrEmpty(callbackUrl)) {
            request.setFrontendURL(callbackUrl);
        } else {
            request.setFrontendURL(mpuProperties.getSuccessUrl());
        }
        request.setMerchantID(mpuProperties.getMerchantId());
        request.setBackendURL(mpuProperties.getPaymentCallbackUrl());
        return buildModel(request);
    }
}
