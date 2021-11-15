package com.sdm.payment.service;

import com.sdm.core.model.response.MessageResponse;
import com.sdm.payment.model.request.cbpay.CBResponsePaymentOrderRequest;
import com.sdm.payment.model.request.kbzpay.KBZPayResponseRequest;
import com.sdm.payment.model.request.mpu.MPUPaymentInquiryResponseRequest;
import com.sdm.payment.model.request.onepay.OnePayResponseDirectPaymentRequest;
import com.sdm.payment.model.request.uabpay.UABPayResponsePaymentRequest;
import com.sdm.payment.model.request.wavepay.WavePayResponsePaymentRequest;
import com.sdm.payment.model.response.cbpay.CBResponsePaymentOrderResponse;
import com.sdm.payment.model.response.onepay.OnePayResponseDirectPaymentResponse;
import com.sdm.payment.model.response.uabpay.UABPayResponsePaymentResponse;

public interface PaymentCallback {

    OnePayResponseDirectPaymentResponse onePayCallback(OnePayResponseDirectPaymentRequest request);

    CBResponsePaymentOrderResponse cbPayCallback(CBResponsePaymentOrderRequest request);

    UABPayResponsePaymentResponse uabPayCallback(UABPayResponsePaymentRequest request);

    MessageResponse wavePayCallback(WavePayResponsePaymentRequest request);

    MessageResponse mpuPayCallback(MPUPaymentInquiryResponseRequest request);

    String kbzPayCallback(KBZPayResponseRequest request);
}
