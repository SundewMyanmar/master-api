package com.sdm.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.HttpResponse;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.service.ISettingManager;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.HttpRequestManager;
import com.sdm.core.util.LocaleManager;
import com.sdm.payment.config.properties.OnePayProperties;
import com.sdm.payment.exception.CallbackException;
import com.sdm.payment.exception.FailedType;
import com.sdm.payment.model.request.onepay.OnePayCheckTransactionRequest;
import com.sdm.payment.model.request.onepay.OnePayDirectPaymentRequest;
import com.sdm.payment.model.request.onepay.OnePayResponseDirectPaymentRequest;
import com.sdm.payment.model.request.onepay.OnePayVerifyPhRequest;
import com.sdm.payment.model.response.onepay.*;
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
public class OnePayPaymentService extends BasePaymentService {
    @Override
    public String getPayment() {
        return "ONE_PAY";
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

    private OnePayProperties getProperties() {
        OnePayProperties properties = new OnePayProperties();
        try {
            properties = settingManager.loadSetting(OnePayProperties.class);
        } catch (IOException | IllegalAccessException ex) {
            log.error(ex.getLocalizedMessage());
        }
        return properties;
    }

    public String generateHash(String stringData) {
        String encryptData = securityManager.generateHashHmac(stringData, this.getProperties().getSecretKey(), "HmacSHA1");
        return encryptData;
    }

    public OnePayVerifyPhResponse verifyPhone(String phoneNo) {
        try {
            OnePayVerifyPhRequest request = new OnePayVerifyPhRequest(this.getProperties().getChannel(), this.getProperties().getUser(),
                    this.getProperties().getPhoneNo(Globalizer.cleanPhoneNo(phoneNo)),
                    "");
            request.setHashValue(generateHash(request.getSignatureString()));
            String rawUrl = this.getProperties().getVerifyPhoneUrl();

            String requestString = objectMapper.writeValueAsString(request);
            writeLog(LogType.REQUEST, requestString);
            HttpResponse serverResponse = httpRequestManager.jsonPostRequest(new URL(rawUrl), requestString, true);
            writeLog(LogType.RESPONSE, serverResponse.getBody());

            OnePayVerifyPhResponse response = objectMapper.readValue(serverResponse.getBody(), OnePayVerifyPhResponse.class);

            //TODO: check hash value response
            String resultHash = generateHash(response.getSignatureString());
            if (!resultHash.equals(response.getHashValue())) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-hash-value", getPayment()));
            }

            if (!response.getRespCode().equals(ApiResponseStatus.SUCCESS.getValue())) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + response.getRespDescription());
            }

            return response;
        } catch (JsonProcessingException | MalformedURLException ex) {
            writeLog(LogType.ERROR, ex.getLocalizedMessage());
            throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + ex.getLocalizedMessage());
        }
    }

    public OnePayDirectPaymentResponse paymentRequest(String invoiceNo, String sequenceNo, String amount, String remark, String walletUserId) {
        try {
            OnePayDirectPaymentRequest request = new OnePayDirectPaymentRequest(this.getProperties().getVersion(), this.getProperties().getChannel(),
                    this.getProperties().getUser(), invoiceNo, sequenceNo, amount, remark,
                    this.getProperties().getPhoneNo(Globalizer.cleanPhoneNo(walletUserId)),
                    this.getProperties().getPaymentCallbackUrl(),
                    this.getProperties().getExpSeconds(), "");
            request.setHashValue(generateHash(request.getSignatureString()));
            String rawUrl = this.getProperties().getDirectPaymentUrl();

            String requestString = objectMapper.writeValueAsString(request);
            writeLog(LogType.REQUEST, requestString);

            HttpResponse serverResponse = httpRequestManager.jsonPostRequest(new URL(rawUrl), requestString, true);
            writeLog(LogType.RESPONSE, serverResponse.getBody());

            OnePayDirectPaymentResponse response = objectMapper.readValue(serverResponse.getBody(), OnePayDirectPaymentResponse.class);

            //TODO: check hash value response
            String resultHash = generateHash(response.getSignatureString());
            if (!resultHash.equals(response.getHashValue())) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-hash-value", getPayment()));
            }

            if (!response.getRespCode().equals(ApiResponseStatus.SUCCESS.getValue())) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + response.getRespDescription());
            }

            return response;
        } catch (JsonProcessingException | MalformedURLException ex) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + ex.getLocalizedMessage());
        }
    }

    public Map<String, String> buildItem(String id, String qty, String price) {
        Map<String, String> item = new HashMap<>();
        item.put("ItemId", id);
        item.put("Quantity", qty);
        item.put("EachPrice", price);
        return item;
    }

    public OnePayResponseDirectPaymentResponse paymentCallback(OnePayResponseDirectPaymentRequest request, List<Map<String, String>> itemList) throws CallbackException {
        try {
            writeLog(LogType.CALLBACK_REQUEST, objectMapper.writeValueAsString(request));

            String requestHash = generateHash(request.getSignatureString());
            if (!requestHash.equals(request.getHashValue())) {
                throw new CallbackException(FailedType.INVALID_HASH, localeManager.getMessage("invalid-hash-value", getPayment()));
            }

            if (!request.getTransactionStatus().equals(ApiResponseStatus.SUCCESS.getValue())) {
                throw new CallbackException(FailedType.TRANSACTION_FAILED, localeManager.getMessage("transaction-failed", getPayment()));
            }

            //TODO: call repository with invoice no and update status
            //TODO: check response transaction status code "000" success
            String itemListStr = objectMapper.writeValueAsString(itemList);
            itemListStr = itemListStr.replace("[", "");
            itemListStr = itemListStr.replace("]", "");
            OnePayResponseDirectPaymentResponse response = new OnePayResponseDirectPaymentResponse(request.getReferIntegrationId(), OnePayResponseDirectPaymentResponse.DataType.Data, "", itemListStr, "Success", TransactionStatus.SUCCESS.getValue(), "");
            response.setHashValue(generateHash(response.getSignatureString()));
            writeLog(LogType.CALLBACK_RESPONSE, objectMapper.writeValueAsString(response));
            return response;
        } catch (JsonProcessingException ex) {
            writeLog(LogType.ERROR, ex.getLocalizedMessage());
            throw new CallbackException(FailedType.RUNTIME_ERROR, ex.getLocalizedMessage(), ex);
        }
    }

    public OnePayCheckTransactionResponse checkStatus(String referIntegrationId) {
        try {
            OnePayCheckTransactionRequest request = new OnePayCheckTransactionRequest(this.getProperties().getUser(), referIntegrationId, "");
            request.setHashValue(generateHash(request.getSignatureString()));
            String rawUrl = this.getProperties().getCheckTransactionUrl();

            String requestString = objectMapper.writeValueAsString(request);
            writeLog(LogType.REQUEST, requestString);

            HttpResponse serverResponse = httpRequestManager.jsonPostRequest(new URL(rawUrl), objectMapper.writeValueAsString(request), true);
            writeLog(LogType.RESPONSE, serverResponse.getBody());

            OnePayCheckTransactionResponse response = objectMapper.readValue(serverResponse.getBody(), OnePayCheckTransactionResponse.class);

            //TODO: check hash value response
            String resultHash = generateHash(response.getSignatureString());
            if (!resultHash.equals(response.getHashValue())) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-hash-value", getPayment()));
            }

            if (!response.getRespCode().equals(ApiResponseStatus.SUCCESS.getValue())) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + response.getRespDescription());
            }

            return response;
        } catch (JsonProcessingException | MalformedURLException ex) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + ex.getLocalizedMessage());
        }
    }
}
