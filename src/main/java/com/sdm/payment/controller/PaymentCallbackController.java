package com.sdm.payment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sdm.core.controller.DefaultController;
import com.sdm.core.exception.GeneralException;
import com.sdm.payment.config.properties.MPUProperties;
import com.sdm.payment.model.request.cbpay.CBResponsePaymentOrderRequest;
import com.sdm.payment.model.request.kbzpay.KBZPayResponseRequest;
import com.sdm.payment.model.request.mpu.MPUPaymentInquiryResponseRequest;
import com.sdm.payment.model.request.onepay.OnePayResponseDirectPaymentRequest;
import com.sdm.payment.model.request.sai2pay.Sai2PayResponsePaymentRequest;
import com.sdm.payment.model.request.wavepay.WavePayResponsePaymentRequest;
import com.sdm.payment.model.response.cbpay.CBResponsePaymentOrderResponse;
import com.sdm.payment.model.response.onepay.OnePayResponseDirectPaymentResponse;
import com.sdm.payment.model.response.sai2pay.Sai2PayResponsePaymentResponse;
import com.sdm.payment.service.PaymentCallback;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@Log4j2
@RestController
@RequestMapping("/public/payments")
public class PaymentCallbackController extends DefaultController {

    @Autowired(required = false)
    private PaymentCallback paymentCallback;

    @Autowired
    private MPUProperties mpuProperties;

    @PostMapping("/onepay/callback")
    public OnePayResponseDirectPaymentResponse onePayCallback(@Valid @RequestBody OnePayResponseDirectPaymentRequest request) throws IOException {
        if (paymentCallback == null) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, localeManager.getMessage("invalid-payment-callback"));
        }
        return paymentCallback.onePayCallback(request);
    }

    @PostMapping("/cb/callback")
    public CBResponsePaymentOrderResponse cbPayCallback(@Valid @RequestBody CBResponsePaymentOrderRequest request) throws JsonProcessingException, NoSuchAlgorithmException {
        if (paymentCallback == null) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, localeManager.getMessage("invalid-payment-callback"));
        }
        log.info("CB_CALLBACK=>" + request.getGenerateRefOrder());
        return paymentCallback.cbPayCallback(request);
    }

    @PostMapping("/sai2/callback")
    public Sai2PayResponsePaymentResponse sai2Callback(@Valid @RequestBody Sai2PayResponsePaymentRequest request) throws IOException {
        if (paymentCallback == null) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, localeManager.getMessage("invalid-payment-callback"));
        }
        log.info("SAISAI_CALLBACK=>" + request);

        return paymentCallback.sai2PayCallback(request);
    }

    @PostMapping("/wave/callback")
    public ResponseEntity<?> waveCallback(@Valid @RequestBody WavePayResponsePaymentRequest request) throws JsonProcessingException {
        if (paymentCallback == null) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, localeManager.getMessage("invalid-payment-callback"));
        }
        return paymentCallback.wavePayCallback(request);
    }

    @PostMapping(value = "/mpu/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> mpuPayCallback(@ModelAttribute MPUPaymentInquiryResponseRequest request) {
        if (paymentCallback == null) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, localeManager.getMessage("invalid-payment-callback"));
        }

        return paymentCallback.mpuPayCallback(request);
    }

    @PostMapping("/kbz/callback")
    public ResponseEntity<?> kbzPayCallback(@RequestBody KBZPayResponseRequest request) {
        if (paymentCallback == null) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, localeManager.getMessage("invalid-payment-callback"));
        }
        return paymentCallback.kbzPayCallback(request);
    }
}


