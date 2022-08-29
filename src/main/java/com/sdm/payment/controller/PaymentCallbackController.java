package com.sdm.payment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.util.LocaleManager;
import com.sdm.payment.model.request.cbpay.CBResponsePaymentOrderRequest;
import com.sdm.payment.model.request.kbzpay.KBZPayResponseRequest;
import com.sdm.payment.model.request.mpu.MPUPaymentInquiryResponseRequest;
import com.sdm.payment.model.request.onepay.OnePayResponseDirectPaymentRequest;
import com.sdm.payment.model.request.uabpay.UABPayResponsePaymentRequest;
import com.sdm.payment.model.request.wavepay.WavePayResponsePaymentRequest;
import com.sdm.payment.model.response.ayapay.AYAPayCallbackResponse;
import com.sdm.payment.model.response.cbpay.CBResponsePaymentOrderResponse;
import com.sdm.payment.model.response.onepay.OnePayResponseDirectPaymentResponse;
import com.sdm.payment.model.response.uabpay.UABPayResponsePaymentResponse;
import com.sdm.payment.service.AYAPayPaymentService;
import com.sdm.payment.service.PaymentCallback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.validation.Valid;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/public/payments")
public class PaymentCallbackController {

    @Autowired(required = false)
    private PaymentCallback paymentCallback;

    @Autowired
    private LocaleManager localeManager;

    @Autowired
    private AYAPayPaymentService ayaService;

    @PostMapping("/onepay/callback")
    public ResponseEntity<OnePayResponseDirectPaymentResponse> onePayCallback(@Valid @RequestBody OnePayResponseDirectPaymentRequest request) throws IOException {
        if (paymentCallback == null) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, localeManager.getMessage("invalid-payment-callback"));
        }
        OnePayResponseDirectPaymentResponse response = paymentCallback.onePayCallback(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cb/callback")
    public ResponseEntity<CBResponsePaymentOrderResponse> cbPayCallback(@Valid @RequestBody CBResponsePaymentOrderRequest request) throws JsonProcessingException, NoSuchAlgorithmException {
        if (paymentCallback == null) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, localeManager.getMessage("invalid-payment-callback"));
        }
        CBResponsePaymentOrderResponse response = paymentCallback.cbPayCallback(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/uab/callback")
    public ResponseEntity<UABPayResponsePaymentResponse> uabPayCallback(@Valid @RequestBody UABPayResponsePaymentRequest request) throws IOException {
        if (paymentCallback == null) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, localeManager.getMessage("invalid-payment-callback"));
        }

        UABPayResponsePaymentResponse response = paymentCallback.uabPayCallback(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/wave/callback")
    public ResponseEntity<MessageResponse> waveCallback(@Valid @RequestBody WavePayResponsePaymentRequest request) throws JsonProcessingException {
        if (paymentCallback == null) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, localeManager.getMessage("invalid-payment-callback"));
        }

        MessageResponse response = paymentCallback.wavePayCallback(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/mpu/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<MessageResponse> mpuPayCallback(@ModelAttribute MPUPaymentInquiryResponseRequest request) {
        if (paymentCallback == null) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, localeManager.getMessage("invalid-payment-callback"));
        }

        MessageResponse response = paymentCallback.mpuPayCallback(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/kbz/callback", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> kbzPayCallback(@RequestBody KBZPayResponseRequest request) {
        if (paymentCallback == null) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, localeManager.getMessage("invalid-payment-callback"));
        }
        String response = paymentCallback.kbzPayCallback(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/aya/callback", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<?> ayaPayCallback(@ModelAttribute AYAPayCallbackResponse request) {
        log.info("AYA_CALLBACK => ", request.getPaymentResult());
        if (paymentCallback == null) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, localeManager.getMessage("invalid-payment-callback"));
        }
        String response = paymentCallback.ayaPayCallback(request);
        return ResponseEntity.ok(response);
    }
}


