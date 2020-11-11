package com.sdm.payment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sdm.payment.model.request.YOMAResponsePaymentRequest;
import com.sdm.payment.model.response.YOMAPaymentResponse;
import com.sdm.payment.service.YOMAPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@RestController
@RequestMapping("/yoma/payments")
public class YOMAPaymentController {
    /**
     * WAVEPay
     * TODO: TRANSACTION CANCEL, TIMEOUT, SYSTEM_ERROR
     */
    @Autowired
    YOMAPaymentService yomaPaymentService;

    @PostMapping("/order")
    public ResponseEntity<?> yomaPaymentOrder() throws IOException, KeyManagementException, NoSuchAlgorithmException {
        //TODO: regenerate merchant reference Id for every new order request
        String merchantReferenceId=UUID.randomUUID().toString().replace("-", "");
        YOMAPaymentResponse response=yomaPaymentService.requestPayment("1",merchantReferenceId,"1000","Test","");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/public/order/callback")
    public ResponseEntity paymentCallback(@Valid @RequestBody YOMAResponsePaymentRequest request) throws JsonProcessingException {
        return yomaPaymentService.requestPaymentCallback(request);
    }
}
