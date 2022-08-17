package com.sdm.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.HttpResponse;
import com.sdm.core.util.ISettingManager;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.HttpRequestManager;
import com.sdm.core.util.LocaleManager;
import com.sdm.payment.config.properties.KBZPayProperties;
import com.sdm.payment.exception.CallbackException;
import com.sdm.payment.exception.FailedType;
import com.sdm.payment.model.request.kbzpay.KBZPayBizContent;
import com.sdm.payment.model.request.kbzpay.KBZPayRequest;
import com.sdm.payment.model.request.kbzpay.KBZPaymentRequest;
import com.sdm.payment.model.request.kbzpay.KBZPaymentResponseRequest;
import com.sdm.payment.model.response.PaymentResultAction;
import com.sdm.payment.model.response.kbzpay.KBZPayPaymentResponse;
import com.sdm.payment.model.response.kbzpay.KBZPayResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Log4j2
@Service
public class KBZPaymentService extends BasePaymentService {

    @Override
    public String getPayment() {
        return "KBZ_PAY";
    }

    private static final String ENCRYPTION_ALGO = "SHA256";
    private static final String CURRENCY = "MMK";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HttpRequestManager httpRequestManager;

    @Autowired
    private LocaleManager localeManager;

    @Autowired
    private ISettingManager settingManager;

    private KBZPayProperties getProperties() {
        KBZPayProperties properties = new KBZPayProperties();
        try {
            properties = settingManager.loadSetting(KBZPayProperties.class);
        } catch (IOException | IllegalAccessException ex) {
            log.error(ex.getLocalizedMessage());
        }
        return properties;
    }

    public String generateHash(String stringData) throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        final byte[] hashbytes = digest.digest(
                stringData.getBytes(StandardCharsets.UTF_8));
        return DatatypeConverter.printHexBinary(hashbytes).toUpperCase(Locale.ROOT);
    }

    public String getStringBuilder(TreeMap<String, String> treeMap) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : treeMap.entrySet()) {
            if (!Globalizer.isNullOrEmpty(entry.getValue())) {
                if (builder.length() > 0) builder.append("&");
                builder.append(String.format("%s=%s", entry.getKey(), entry.getValue()));
            }
        }

        builder.append(String.format("&%s=%s", "key", this.getProperties().getSecretKey()));
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

    public Map<String, Object> buildPayment(String prepayId, String nonce, String timestamp) throws NoSuchAlgorithmException {
        TreeMap<String, String> treeMap = new TreeMap<>();
        treeMap.put("appid", this.getProperties().getAppId());
        treeMap.put("merch_code", this.getProperties().getMerchantCode());
        treeMap.put("nonce_str", nonce);
        treeMap.put("prepay_id", prepayId);
        treeMap.put("timestamp", timestamp);
        String orderInfo = this.getStringBuilder(treeMap);
        String sign = generateHash(orderInfo);
        return Map.of(
                "referenceIntegrationId", prepayId,
                "orderInfo", orderInfo,
                "type", PaymentResultAction.PRE_PAY_ID,
                "sign", sign
        );
    }

    public KBZPayBizContent getBizContent(String invoiceNo, Double amount) {
        KBZPayBizContent bizContent = new KBZPayBizContent(this.getProperties().getMerchantCode(), invoiceNo,
                this.getProperties().getAppId(), "APP", String.format("Payment for %s", invoiceNo),
                amount.toString(), KBZPaymentService.CURRENCY, invoiceNo);
        return bizContent;
    }

    public Map<String, Object> paymentRequest(String invoiceNo, Double amount) {
        try {
            KBZPayRequest request = new KBZPayRequest(String.valueOf(new Date().getTime()), this.getProperties().getPaymentCallbackUrl(),
                    KBZPayProperties.PaymentMethod.CREATE.getValue(), invoiceNo,
                    KBZPaymentService.ENCRYPTION_ALGO, "",
                    this.getProperties().getVersion(), this.getBizContent(invoiceNo, amount));
            String signatureString = this.getSignatureString(request);
            request.setSign(generateHash(signatureString));

            String rawUrl = this.getProperties().getPaymentOrderUrl();

            KBZPaymentRequest paymentRequest = new KBZPaymentRequest(request);
            String paymentRequestString = objectMapper.writeValueAsString(paymentRequest);
            writeLog(LogType.REQUEST, paymentRequestString);
            HttpResponse serverResponse = httpRequestManager.jsonPostRequest(new URL(rawUrl), paymentRequestString, !this.getProperties().getIsUat());
            writeLog(LogType.RESPONSE, serverResponse.getBody());

            KBZPayPaymentResponse paymentResult = objectMapper.readValue(serverResponse.getBody(), KBZPayPaymentResponse.class);
            KBZPayResponse response = paymentResult.getResponse();

            if (response == null || !response.getCode().equals("0")) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + response.getMsg());
            }

            return this.buildPayment(response.getPrepayId(), response.getNonceStr(), request.getTimestamp());
        } catch (MalformedURLException | JsonProcessingException | NoSuchAlgorithmException ex) {
            writeLog(LogType.ERROR, ex.getLocalizedMessage());
            throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + ex.getLocalizedMessage());
        }
    }

    public String paymentCallback(KBZPaymentResponseRequest request) throws CallbackException {
        try {
            writeLog(LogType.CALLBACK_REQUEST, objectMapper.writeValueAsString(request));
            String signatureString = getCallbackSignatureString(request);
            String sign = generateHash(signatureString);

            if (!sign.equals(request.getSign())) {
                throw new CallbackException(FailedType.INVALID_HASH, localeManager.getMessage("invalid-hash-value", getPayment()));
            }

            if (!request.getTradeStatus().equals("PAY_SUCCESS")) {
                throw new CallbackException(FailedType.TRANSACTION_FAILED, localeManager.getMessage("transaction-failed", getPayment()));
            }

            return "success";
        } catch (NoSuchAlgorithmException | JsonProcessingException ex) {
            writeLog(LogType.ERROR, ex.getLocalizedMessage());
            throw new CallbackException(FailedType.RUNTIME_ERROR, ex.getLocalizedMessage(), ex);
        }
    }
}
