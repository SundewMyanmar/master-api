package com.sdm.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.HttpRequestManager;
import com.sdm.core.util.LocaleManager;
import com.sdm.payment.config.properties.KBZPayProperties;
import com.sdm.payment.model.request.kbzpay.KBZPayBizContent;
import com.sdm.payment.model.request.kbzpay.KBZPayRequest;
import com.sdm.payment.model.request.kbzpay.KBZPaymentRequest;
import com.sdm.payment.model.request.kbzpay.KBZPaymentResponseRequest;
import com.sdm.payment.model.response.PaymentResultAction;
import com.sdm.payment.model.response.kbzpay.KBZPayPaymentResponse;
import com.sdm.payment.model.response.kbzpay.KBZPayResponse;
import com.sdm.payment.util.PaymentSecurityManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Log4j2
@Service
public class KBZPaymentService {

    private static final String ENCRYPTION_ALGO = "SHA256";
    private static final String CURRENCY = "MMK";

    @Autowired
    private KBZPayProperties kbzPayProperties;

    @Autowired
    private PaymentSecurityManager paymentSecurityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HttpRequestManager httpRequestManager;

    @Autowired
    private LocaleManager localeManager;

    public String getStringBuilder(TreeMap<String, String> treeMap) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : treeMap.entrySet()) {
            if (!Globalizer.isNullOrEmpty(entry.getValue())) {
                if (builder.length() > 0) builder.append("&");
                builder.append(String.format("%s=%s", entry.getKey(), entry.getValue()));
            }
        }

        builder.append(String.format("&%s=%s", "key", kbzPayProperties.getSecretKey()));
        return builder.toString();
    }

    public String getSignatureString(KBZPayRequest request) {
        TreeMap<String, String> treeMap = new TreeMap<>();
        treeMap.put("timestamp", request.getTimestamp());
        treeMap.put("notify_url", request.getNotifyUrl());
        treeMap.put("method", request.getMethod());
        treeMap.put("nonce_str", request.getNonceStr());
        treeMap.put("version", request.getVersion());

        KBZPayBizContent bizContent = request.getBizContent();
        treeMap.put("merch_code", bizContent.getMerchCode());
        treeMap.put("merch_order_id", bizContent.getMerchOrderId());
        treeMap.put("appid", bizContent.getAppId());
        treeMap.put("trade_type", bizContent.getTradeType());
        treeMap.put("title", bizContent.getTitle());
        treeMap.put("total_amount", bizContent.getTotalAmount());
        treeMap.put("trans_currency", bizContent.getTransCurrency());
        treeMap.put("callback_info", bizContent.getCallbackInfo());

        return this.getStringBuilder(treeMap);
    }

    public String getCallbackSignatureString(KBZPaymentResponseRequest request) {
        TreeMap<String, String> treeMap = new TreeMap<>();
        treeMap.put("appid", request.getAppId());
        treeMap.put("notify_time", request.getNotifyTime());
        treeMap.put("merch_code", request.getMerchCode());
        treeMap.put("merch_order_id", request.getMerchOrderId());
        treeMap.put("mm_order_id", request.getMmOrderId());
        treeMap.put("total_amount", request.getTotalAmount());
        treeMap.put("trans_currency", request.getTransCurrency());
        treeMap.put("trade_status", request.getTradeStatus());
        treeMap.put("trans_end_time", request.getTransEndTime());
        treeMap.put("callback_info", request.getCallbackInfo());
        treeMap.put("nonce_str", request.getNonceStr());

        return this.getStringBuilder(treeMap);
    }

    public Map<String, Object> buildPayment(KBZPayResponse prepay, String timestamp) throws NoSuchAlgorithmException {
        TreeMap<String, String> treeMap = new TreeMap<>();
        treeMap.put("appid", kbzPayProperties.getAppId());
        treeMap.put("merch_code", kbzPayProperties.getMerchantCode());
        treeMap.put("nonce_str", prepay.getNonceStr());
        treeMap.put("prepay_id", prepay.getPrepayId());
        treeMap.put("timestamp", timestamp);
        String orderInfo = this.getStringBuilder(treeMap);
        String sign = paymentSecurityManager.generateKbzPayHashSHA256(orderInfo).toUpperCase(Locale.ROOT);
        return Map.of(
                "orderInfo", orderInfo,
                "type", PaymentResultAction.PRE_PAY_ID,
                "sign", sign
        );
    }

    public KBZPayBizContent getBizContent(String invoiceNo, Double amount) {
        KBZPayBizContent bizContent = new KBZPayBizContent(kbzPayProperties.getMerchantCode(), invoiceNo,
                kbzPayProperties.getAppId(), "APP", String.format("Payment for %s", invoiceNo),
                amount.toString(), KBZPaymentService.CURRENCY, invoiceNo);
        return bizContent;
    }

    public Map<String, Object> requestPayment(String invoiceNo, Double amount) throws NoSuchAlgorithmException {
        KBZPayRequest request = new KBZPayRequest(String.valueOf(new Date().getTime()), kbzPayProperties.getPaymentCallbackUrl(),
                KBZPayProperties.PaymentMethod.CREATE.getValue(), invoiceNo,
                KBZPaymentService.ENCRYPTION_ALGO, "",
                kbzPayProperties.getVersion(), this.getBizContent(invoiceNo, amount));
        String signatureString = this.getSignatureString(request);
        request.setSign(paymentSecurityManager.generateKbzPayHashSHA256(signatureString).toUpperCase(Locale.ROOT));

        String rawUrl = kbzPayProperties.getPaymentOrderUrl();

        String resultString = "";
        KBZPayPaymentResponse paymentResult = new KBZPayPaymentResponse();
        KBZPaymentRequest paymentRequest = new KBZPaymentRequest(request);
        try {
            resultString = httpRequestManager.jsonPostRequest(new URL(rawUrl), objectMapper.writeValueAsString(paymentRequest), !kbzPayProperties.getIsUat());
            paymentResult = objectMapper.readValue(resultString, KBZPayPaymentResponse.class);
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("unprocessable-payment-response"));
        }

        KBZPayResponse response = paymentResult.getResponse();
        if (response == null || !response.getCode().equals("0")) {
            log.error("INVALID_KBZ_RESPONSE >>> " + resultString);
            throw new GeneralException(HttpStatus.BAD_REQUEST, response.getMsg());
        }

        return this.buildPayment(response, request.getTimestamp());
    }

    private void writeLog(KBZPaymentResponseRequest request) {
        try {
            log.error("INVALID_KBZ_RESPONSE >>>" + objectMapper.writeValueAsString(request));
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("unprocessable-payment-response"));
        }
    }

    public ResponseEntity<?> paymentRequestCallback(KBZPaymentResponseRequest request) {
        try {
            String signatureString = getCallbackSignatureString(request);
            String sign = paymentSecurityManager.generateKbzPayHashSHA256(signatureString).toUpperCase(Locale.ROOT);

            if (!sign.equals(request.getSign())) {
                writeLog(request);
                throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-payment-server-response"));
            }

            if (!request.getTradeStatus().equals("PAY_SUCCESS")) {
                writeLog(request);
                throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("unprocessable-payment-response"));
            }

            return ResponseEntity.ok(new MessageResponse(localeManager.getMessage("success"), localeManager.getMessage("kpay-callback-success", request.getMerchOrderId())));
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
        }
    }
}
