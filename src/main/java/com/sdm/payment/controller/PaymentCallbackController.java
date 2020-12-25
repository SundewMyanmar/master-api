package com.sdm.payment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sdm.core.exception.GeneralException;
import com.sdm.payment.model.request.cbpay.CBResponsePaymentOrderRequest;
import com.sdm.payment.model.request.onepay.OnePayResponseDirectPaymentRequest;
import com.sdm.payment.model.request.sai2pay.Sai2PayResponsePaymentRequest;
import com.sdm.payment.model.request.wavepay.WavePayResponsePaymentRequest;
import com.sdm.payment.model.response.cbpay.CBResponsePaymentOrderResponse;
import com.sdm.payment.model.response.onepay.OnePayResponseDirectPaymentResponse;
import com.sdm.payment.model.response.sai2pay.Sai2PayResponsePaymentResponse;
import com.sdm.payment.service.PaymentCallback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/public/payments")
public class PaymentCallbackController {
	
    @Autowired(required = false)
    private PaymentCallback paymentCallback;

    @PostMapping("/onepay/callback")
    public OnePayResponseDirectPaymentResponse onePayCallback(@Valid @RequestBody OnePayResponseDirectPaymentRequest request) throws IOException {
    	if(paymentCallback == null) {
    		throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment callback service is not found.");
    	}
        return paymentCallback.onePayCallback(request);
    }

    @PostMapping("/cb/callback")
    public CBResponsePaymentOrderResponse cbPayCallback(@Valid @RequestBody CBResponsePaymentOrderRequest request) throws JsonProcessingException, NoSuchAlgorithmException {
    	if(paymentCallback == null) {
    		throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment callback service is not found.");
    	}
        return paymentCallback.cbPayCallback(request);
    }

    @PostMapping("/sai2/callback")
    public Sai2PayResponsePaymentResponse sai2Callback(@Valid @RequestBody Sai2PayResponsePaymentRequest request) throws IOException {
    	if(paymentCallback == null) {
    		throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment callback service is not found.");
    	}
        return paymentCallback.sai2PayCallback(request);
    }

    @PostMapping("/wave/callback")
    public ResponseEntity<?> waveCallback(@Valid @RequestBody WavePayResponsePaymentRequest request) throws JsonProcessingException {
    	if(paymentCallback == null) {
    		throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment callback service is not found.");
    	}
        return paymentCallback.wavePayCallback(request);
    }
}
