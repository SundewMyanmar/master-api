package com.sdm.payment.controller;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sdm.payment.model.request.wavepay.WavePayResponsePaymentRequest;
import com.sdm.payment.model.response.wavepay.WavePayPaymentResponse;
import com.sdm.payment.service.WavePayPaymentService;

@RestController
@RequestMapping("/yoma/payments")
public class YOMAPaymentController {
    /**
     * WAVEPay
     * TODO: TRANSACTION CANCEL, TIMEOUT, SYSTEM_ERROR
     */
    @Autowired
    WavePayPaymentService wavePayPaymentService;

    @PostMapping("/order")
    public ResponseEntity<?> yomaPaymentOrder() throws IOException, KeyManagementException, NoSuchAlgorithmException {
        //TODO: regenerate merchant reference Id for every new order request
        String merchantReferenceId = UUID.randomUUID().toString().replace("-", "");
        WavePayPaymentResponse response = wavePayPaymentService.requestPayment("1", merchantReferenceId, "1000", "Test", "");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/public/order/callback")
    public ResponseEntity paymentCallback(@Valid @RequestBody WavePayResponsePaymentRequest request) throws JsonProcessingException {
        return wavePayPaymentService.requestPaymentCallback(request);
    }
}
