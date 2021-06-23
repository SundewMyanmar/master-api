package com.sdm.payment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.util.Globalizer;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@Log4j2
@RestController
@RequestMapping("/public/payments")
public class PaymentCallbackController {

    @Autowired(required = false)
    private PaymentCallback paymentCallback;

    @Autowired
    private MPUProperties mpuProperties;

    @GetMapping("/test")
    public ResponseEntity<?> testcallback() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();

        String uri2 = request.getScheme() + "s://" +   // "http" + "://
                request.getServerName() +       // "myhost"
                ":" + request.getServerPort() + "/public/payments/cb/callback";

        String uri3 = "https://www.gooogle.com".replace("http:", "https:");
        String uri4 = Globalizer.getCurrentContextPath("/public/payments/cb/callback", true);
        ListResponse<String> response = new ListResponse<>();
        response.addData("2 : " + uri2);
        response.addData("3 : " + uri3);
        response.addData("4 Globalizer : " + uri4);
        response.addData("test" + ServletUriComponentsBuilder.fromCurrentContextPath().scheme("https").path("/public/payments/cb/callback").toUriString());
        String testUrl = mpuProperties.getPaymentCallbackUrl() + "?invoiceNo=" + Globalizer.encodeUrl("202020120");
        response.addData("ttt" + testUrl);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/onepay/callback")
    public OnePayResponseDirectPaymentResponse onePayCallback(@Valid @RequestBody OnePayResponseDirectPaymentRequest request) throws IOException {
        if (paymentCallback == null) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment callback service is not found.");
        }
        return paymentCallback.onePayCallback(request);
    }

    @PostMapping("/cb/callback")
    public CBResponsePaymentOrderResponse cbPayCallback(@Valid @RequestBody CBResponsePaymentOrderRequest request) throws JsonProcessingException, NoSuchAlgorithmException {
        if (paymentCallback == null) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment callback service is not found.");
        }
        log.info("CB_CALLBACK=>" + request.getGenerateRefOrder());
        return paymentCallback.cbPayCallback(request);
    }

    @PostMapping("/sai2/callback")
    public Sai2PayResponsePaymentResponse sai2Callback(@Valid @RequestBody Sai2PayResponsePaymentRequest request) throws IOException {
        if (paymentCallback == null) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment callback service is not found.");
        }
        log.info("SAISAI_CALLBACK=>" + request);

        return paymentCallback.sai2PayCallback(request);
    }

    @PostMapping("/wave/callback")
    public ResponseEntity<?> waveCallback(@Valid @RequestBody WavePayResponsePaymentRequest request) throws JsonProcessingException {
        if (paymentCallback == null) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment callback service is not found.");
        }
        return paymentCallback.wavePayCallback(request);
    }

    @PostMapping(value = "/mpu/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> mpuPayCallback(@ModelAttribute MPUPaymentInquiryResponseRequest request) {
        if (paymentCallback == null) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment callback service is not found.");
        }

        return paymentCallback.mpuPayCallback(request);
    }

    @PostMapping("/kbz/callback")
    public ResponseEntity<?> kbzPayCallback(@RequestBody KBZPayResponseRequest request) {
        if (paymentCallback == null) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment callback service is not found.");
        }
        return paymentCallback.kbzPayCallback(request);
    }
}


