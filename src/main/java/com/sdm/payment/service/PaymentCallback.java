package com.sdm.payment.service;

import com.sdm.payment.model.request.cbpay.CBResponsePaymentOrderRequest;
import com.sdm.payment.model.request.kbzpay.KBZPayResponseRequest;
import com.sdm.payment.model.request.mpu.MPUPaymentInquiryResponseRequest;
import com.sdm.payment.model.request.onepay.OnePayResponseDirectPaymentRequest;
import com.sdm.payment.model.request.sai2pay.Sai2PayResponsePaymentRequest;
import com.sdm.payment.model.request.wavepay.WavePayResponsePaymentRequest;
import com.sdm.payment.model.response.cbpay.CBResponsePaymentOrderResponse;
import com.sdm.payment.model.response.onepay.OnePayResponseDirectPaymentResponse;
import com.sdm.payment.model.response.sai2pay.Sai2PayResponsePaymentResponse;
import org.springframework.http.ResponseEntity;

public interface PaymentCallback {
    OnePayResponseDirectPaymentResponse onePayCallback(OnePayResponseDirectPaymentRequest request);

    CBResponsePaymentOrderResponse cbPayCallback(CBResponsePaymentOrderRequest request);

    Sai2PayResponsePaymentResponse sai2PayCallback(Sai2PayResponsePaymentRequest request);

    ResponseEntity<?> wavePayCallback(WavePayResponsePaymentRequest request);

    ResponseEntity<?> mpuPayCallback(MPUPaymentInquiryResponseRequest request);

    ResponseEntity<?> kbzPayCallback(KBZPayResponseRequest request);
}
