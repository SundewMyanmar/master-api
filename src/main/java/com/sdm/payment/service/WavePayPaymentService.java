package com.sdm.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.HttpResponse;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.ISettingManager;
import com.sdm.core.util.HttpRequestManager;
import com.sdm.core.util.LocaleManager;
import com.sdm.payment.config.properties.WavePayProperties;
import com.sdm.payment.exception.CallbackException;
import com.sdm.payment.exception.FailedType;
import com.sdm.payment.model.request.wavepay.WavePayPaymentRequest;
import com.sdm.payment.model.request.wavepay.WavePayResponsePaymentRequest;
import com.sdm.payment.model.response.wavepay.WavePayPaymentResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class WavePayPaymentService extends BasePaymentService {
    @Override
    public String getPayment() {
        return "WAVE_PAY";
    }

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private HttpRequestManager httpRequestManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LocaleManager localeManager;

    @Autowired
    private ISettingManager settingManager;

    private WavePayProperties getProperties() {
        WavePayProperties properties = new WavePayProperties();
        try {
            properties = settingManager.loadSetting(WavePayProperties.class);
        } catch (IOException | IllegalAccessException ex) {
            log.error(ex.getLocalizedMessage());
        }
        return properties;
    }

    public String generateHash(String stringData) {
        String encryptData = securityManager.generateHashHmac(stringData, this.getProperties().getSecretKey(), "HmacSHA256");
        return encryptData.toLowerCase();
    }

    public Map<String, String> buildItem(String name, String amount) {
        Map<String, String> item = new HashMap<>();
        item.put("name", name);
        item.put("amount", amount);
        return item;
    }

    public WavePayPaymentResponse paymentRequest(String orderId, String merchantReferenceId, Integer amount, String paymentDescription, List<Map<String, String>> itemList) {
        try {
            String itemListStr = objectMapper.writeValueAsString(itemList);

            WavePayPaymentRequest request = new WavePayPaymentRequest(this.getProperties().getMerchantId(),
                    orderId, merchantReferenceId, this.getProperties().getSuccessUrl(),
                    this.getProperties().getPaymentCallbackUrl(),
                    amount, this.getProperties().getExpSeconds().toString(), paymentDescription,
                    this.getProperties().getMerchantName(), itemListStr, "");

            String requestHash = generateHash(request.getSignatureString());
            request.setHash(requestHash);
            String rawUrl = this.getProperties().getPaymentRequestUrl();
            String requestString = objectMapper.writeValueAsString(request);
            writeLog(LogType.REQUEST, requestString);

            HttpResponse serverResponse = httpRequestManager.jsonPostRequest(new URL(rawUrl), requestString, true);
            writeLog(LogType.RESPONSE, serverResponse.getBody());

            WavePayPaymentResponse response = objectMapper.readValue(serverResponse.getBody(), WavePayPaymentResponse.class);
            response.setAuthenticatedUrl(this.getProperties().getPaymentRequestAuthenticateUrl(response.getTransactionId()));

            if (!response.getMessage().equals("success")) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + response.getMessage());
            }

            return response;
        } catch (JsonProcessingException | MalformedURLException ex) {
            writeLog(LogType.ERROR, ex.getLocalizedMessage());
            throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + ex.getLocalizedMessage());
        }
    }

    public MessageResponse paymentCallback(WavePayResponsePaymentRequest request) throws CallbackException {
        try {
            writeLog(LogType.CALLBACK_REQUEST, objectMapper.writeValueAsString(request));
            String requestHash = generateHash(request.getSignatureString());
            if (!requestHash.equals(request.getHashValue())) {
                throw new CallbackException(FailedType.INVALID_HASH, localeManager.getMessage("invalid-hash-value", getPayment()));
            }

            if (!request.getStatus().equals(WavePayResponsePaymentRequest.PaymentStatus.PAYMENT_CONFIRMED)) {
                throw new CallbackException(FailedType.TRANSACTION_FAILED, localeManager.getMessage("transaction-failed", getPayment()));
            }

            return new MessageResponse("SUCCESS", localeManager.getMessage("wave-pay-callback-success", request.getTransactionId()));
        } catch (JsonProcessingException ex) {
            writeLog(LogType.ERROR, ex.getLocalizedMessage());
            throw new CallbackException(FailedType.RUNTIME_ERROR, ex.getLocalizedMessage(), ex);
        }
    }
}
