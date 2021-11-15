package com.sdm.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.HttpResponse;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.HttpRequestManager;
import com.sdm.core.util.LocaleManager;
import com.sdm.core.util.SettingManager;
import com.sdm.payment.config.properties.UABPayProperties;
import com.sdm.payment.exception.CallbackException;
import com.sdm.payment.exception.FailedType;
import com.sdm.payment.model.request.uabpay.*;
import com.sdm.payment.model.response.uabpay.*;
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
public class UABPayPaymentService extends BasePaymentService {
    @Override
    public String getPayment() {
        return "UAB_PAY";
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
    private SettingManager settingManager;

    private UABPayProperties getProperties() {
        UABPayProperties properties = new UABPayProperties();
        try {
            properties = settingManager.loadSetting(UABPayProperties.class);
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
        }
        return properties;
    }

    public String generateHash(String stringData) {
        String encryptData = securityManager.generateHashHmac(stringData, this.getProperties().getSecretKey(), "HmacSHA1");
        return encryptData;
    }

    public UABPayLoginResponse login() {
        try {
            UABPayLoginRequest request = new UABPayLoginRequest(this.getProperties().getUser(), this.getProperties().getPassword());
            String rawUrl = this.getProperties().getLoginUrl();
            String requestString = objectMapper.writeValueAsString(request);
            writeLog(LogType.REQUEST, requestString);

            HttpResponse serverResponse = httpRequestManager.jsonPostRequest(new URL(rawUrl), requestString, false);
            writeLog(LogType.RESPONSE, serverResponse.getBody());
            UABPayLoginResponse response = objectMapper.readValue(serverResponse.getBody(), UABPayLoginResponse.class);

            if (!response.getRespCode().equals(ApiResponseStatus.SUCCESS.getValue())) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + response.getRespDescription());
            }

            return response;
        } catch (JsonProcessingException | MalformedURLException ex) {
            writeLog(LogType.ERROR, ex.getLocalizedMessage());
            throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + ex.getLocalizedMessage());
        }
    }

    // DANGER app.properties need to change if you call this api
    public UABPayLoginResponse changePassword(String oldPassword, String newPassword) {
        try {
            if (!oldPassword.equals(this.getProperties().getPassword())) {
                writeLog(LogType.ERROR, "Invalid old password!");
                throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + localeManager.getMessage("invalid-old-password"));
            }

            UABPayChangePasswordRequest request = new UABPayChangePasswordRequest(this.getProperties().getUser(),
                    this.getProperties().getPassword(), newPassword);
            String rawUrl = this.getProperties().getChangePasswordUrl();
            String requestString = objectMapper.writeValueAsString(request);
            writeLog(LogType.REQUEST, requestString);

            HttpResponse serverResponse = httpRequestManager.jsonPostRequest(new URL(rawUrl), requestString,
                    false);
            writeLog(LogType.RESPONSE, serverResponse.getBody());

            UABPayLoginResponse response = objectMapper.readValue(serverResponse.getBody(), UABPayLoginResponse.class);

            if (!response.getRespCode().equals(ApiResponseStatus.SUCCESS.getValue())) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + response.getRespDescription());
            }

            return response;
        } catch (JsonProcessingException | MalformedURLException ex) {
            writeLog(LogType.ERROR, ex.getLocalizedMessage());
            throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + ex.getLocalizedMessage());
        }
    }

    public UABPayCheckPhResponse verifyPhone(String phoneNo, String tokenString) {
        try {
            UABPayCheckPhRequest request = new UABPayCheckPhRequest(this.getProperties().getChannel(), this.getProperties().getUser(),
                    this.getProperties().getPhoneNo(Globalizer.cleanPhoneNo(phoneNo)),
                    this.getProperties().getAppName(), "");
            request.setHashValue(generateHash(request.getSignatureString()));
            String rawUrl = this.getProperties().getCheckPhoneUrl();
            String requestString = objectMapper.writeValueAsString(request);
            writeLog(LogType.REQUEST, requestString);

            HttpResponse serverResponse = httpRequestManager.jsonPostRequest(new URL(rawUrl), requestString,
                    tokenString, false);
            writeLog(LogType.RESPONSE, serverResponse.getBody());

            UABPayCheckPhResponse response = objectMapper.readValue(serverResponse.getBody(), UABPayCheckPhResponse.class);
            if (!response.getRespCode().equals(ApiResponseStatus.SUCCESS.getValue())) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + response.getRespDescription());
            }

            // TODO: check hash value response
            String resultHash = generateHash(response.getSignatureString());
            if (!resultHash.equals(response.getHashValue())) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-hash-value", getPayment()));
            }

            return response;
        } catch (JsonProcessingException | MalformedURLException ex) {
            writeLog(LogType.ERROR, ex.getLocalizedMessage());
            throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + ex.getLocalizedMessage());
        }
    }

    public UABPayEnquiryPaymentResponse paymentRequest(String invoiceNo, String sequenceNo, String amount,
                                                       String remark, String walletUserId, String tokenString) {
        try {
            UABPayEnquiryPaymentRequest request = new UABPayEnquiryPaymentRequest(this.getProperties().getChannel(),
                    this.getProperties().getAppName(), this.getProperties().getUser(), invoiceNo, sequenceNo, amount, remark,
                    this.getProperties().getPhoneNo(Globalizer.cleanPhoneNo(walletUserId)),
                    this.getProperties().getEnquiryCallbackUrl(), this.getProperties().getExpSeconds(), "");
            request.setHashValue(generateHash(request.getSignatureString()));
            String rawUrl = this.getProperties().getEnquiryPaymentUrl();
            String requestString = objectMapper.writeValueAsString(request);
            writeLog(LogType.REQUEST, requestString);

            HttpResponse serverResponse = httpRequestManager.jsonPostRequest(new URL(rawUrl), requestString,
                    tokenString, false);
            writeLog(LogType.RESPONSE, serverResponse.getBody());
            UABPayEnquiryPaymentResponse response = objectMapper.readValue(serverResponse.getBody(), UABPayEnquiryPaymentResponse.class);

            if (!response.getRespCode().equals(ApiResponseStatus.SUCCESS.getValue())) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + response.getRespDescription());
            }

            // TODO: check hash value response
            String resultHash = generateHash(response.getSignatureString());
            if (!resultHash.equals(response.getHashValue())) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-hash-value", getPayment()));
            }

            return response;
        } catch (JsonProcessingException | MalformedURLException ex) {
            writeLog(LogType.ERROR, ex.getLocalizedMessage());
            throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + ex.getLocalizedMessage());
        }
    }

    public UABPayResponsePaymentResponse paymentCallback(UABPayResponsePaymentRequest request,
                                                         List<Map<String, String>> itemList) throws CallbackException {
        try {
            writeLog(LogType.CALLBACK_REQUEST, objectMapper.writeValueAsString(request));
            String requestHash = generateHash(request.getSignatureString());
            if (!requestHash.equals(request.getHashValue())) {
                throw new CallbackException(FailedType.INVALID_HASH, localeManager.getMessage("invalid-hash-value", getPayment()));
            }

            if (!request.getTransactionStatus().equals(ApiResponseStatus.SUCCESS.getValue())) {
                throw new CallbackException(FailedType.TRANSACTION_FAILED, localeManager.getMessage("transaction-failed", getPayment()));
            }

            // TODO: call repository with invoice no and update status
            // TODO: check response transaction status code "000" success
            String itemListStr = objectMapper.writeValueAsString(itemList);

            UABPayResponsePaymentResponse response = new UABPayResponsePaymentResponse(request.getReferIntegrationId(),
                    UABPayResponsePaymentResponse.DataType.Data, "", itemListStr, "Success",
                    TransactionStatus.SUCCESS.getValue(), "");

            writeLog(LogType.CALLBACK_RESPONSE, objectMapper.writeValueAsString(response));
            response.setHashValue(generateHash(response.getSignatureString()));

            return response;
        } catch (JsonProcessingException ex) {
            writeLog(LogType.ERROR, ex.getLocalizedMessage());
            throw new CallbackException(FailedType.RUNTIME_ERROR, ex.getLocalizedMessage(), ex);
        }
    }

    public Map<String, String> buildItem(String id, String qty, String price) {
        Map<String, String> item = new HashMap<>();
        item.put("ItemId", id);
        item.put("Quantity", qty);
        item.put("EachPrice", price);
        return item;
    }

    public UABPayCheckTransactionResponse checkStatus(String ReferIntegrationID, String tokenString) {
        try {
            UABPayCheckTransactionRequest request = new UABPayCheckTransactionRequest(this.getProperties().getUser(),
                    this.getProperties().getChannel(), this.getProperties().getAppName(), ReferIntegrationID, "");
            request.setHashValue(generateHash(request.getSignatureString()));
            String rawUrl = this.getProperties().getCheckTransactionStatus();
            String requestString = objectMapper.writeValueAsString(request);
            writeLog(LogType.REQUEST, requestString);

            HttpResponse serverResponse = httpRequestManager.jsonPostRequest(new URL(rawUrl), requestString,
                    tokenString, false);
            writeLog(LogType.RESPONSE, serverResponse.getBody());
            UABPayCheckTransactionResponse response = objectMapper.readValue(serverResponse.getBody(), UABPayCheckTransactionResponse.class);
            if (!response.getRespCode().equals(ApiResponseStatus.SUCCESS.getValue())) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + response.getRespDescription());
            }

            // TODO: check hash value response
            String resultHash = generateHash(response.getSignatureString());
            if (!resultHash.equals(response.getHashValue())) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-hash-value", getPayment()));
            }

            return response;
        } catch (JsonProcessingException | MalformedURLException ex) {
            writeLog(LogType.ERROR, ex.getLocalizedMessage());
            throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + ex.getLocalizedMessage());
        }
    }

}
