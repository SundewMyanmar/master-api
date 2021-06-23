package com.sdm.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.util.HttpRequestManager;
import com.sdm.payment.config.properties.Sai2PayProperties;
import com.sdm.payment.model.request.sai2pay.*;
import com.sdm.payment.model.response.sai2pay.*;
import com.sdm.payment.util.PaymentSecurityManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class Sai2PayPaymentService {
    @Autowired
    private PaymentSecurityManager securityManager;

    @Autowired
    private Sai2PayProperties uabProperties;

    @Autowired
    private HttpRequestManager httpRequestManager;

    @Autowired
    private ObjectMapper objectMapper;

    public Sai2PayLoginResponse login() {
        Sai2PayLoginRequest request = new Sai2PayLoginRequest(uabProperties.getUser(), uabProperties.getPassword());
        String rawUrl = uabProperties.getLoginUrl();

        String resultString = "";
        Sai2PayLoginResponse result = new Sai2PayLoginResponse();
        try {
            resultString = httpRequestManager.jsonPostRequest(new URL(rawUrl), objectMapper.writeValueAsString(request), false);
            result = objectMapper.readValue(resultString, Sai2PayLoginResponse.class);
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_GATEWAY, "Payment server return unprocessable entity.");
        }

        if (!result.getRespCode().equals(ApiResponseStatus.SUCCESS.getValue())) {
            log.error(resultString);
            throw new GeneralException(HttpStatus.BAD_GATEWAY, result.getRespDescription());
        }

        return result;
    }

    // DANGER app.properties need to change if you call this api
    public Sai2PayLoginResponse changePassword(String oldPassword, String newPassword) {
        if (!oldPassword.equals(uabProperties.getPassword())) {
            log.error("Invalid Old Password : " + oldPassword);
            throw new GeneralException(HttpStatus.BAD_GATEWAY, "Invalid Old Password!");
        }

        Sai2PayChangePasswordRequest request = new Sai2PayChangePasswordRequest(uabProperties.getUser(),
                uabProperties.getPassword(), newPassword);
        String rawUrl = uabProperties.getChangePasswordUrl();

        String resultString = "";
        Sai2PayLoginResponse result = new Sai2PayLoginResponse();
        try {
            resultString = httpRequestManager.jsonPostRequest(new URL(rawUrl), objectMapper.writeValueAsString(request),
                    false);
            result = objectMapper.readValue(resultString, Sai2PayLoginResponse.class);
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_GATEWAY, "Payment server return unprocessable entity.");
        }

        if (!result.getRespCode().equals(ApiResponseStatus.SUCCESS.getValue())) {
            log.error(resultString);
            throw new GeneralException(HttpStatus.BAD_GATEWAY, result.getRespDescription());
        }

        return result;
    }

    public Sai2PayCheckPhResponse verifyPhone(String phoneNo, String tokenString) {
        Sai2PayCheckPhRequest request = new Sai2PayCheckPhRequest(uabProperties.getChannel(), uabProperties.getUser(),
                phoneNo, uabProperties.getAppName(), "");
        request.setHashValue(securityManager.generateUABHashHmac(request.getSignatureString()));
        String rawUrl = uabProperties.getCheckPhoneUrl();

        String resultString = "";
        Sai2PayCheckPhResponse result = new Sai2PayCheckPhResponse();
        try {
            resultString = httpRequestManager.jsonPostRequest(new URL(rawUrl), objectMapper.writeValueAsString(request),
                    tokenString, false);
            result = objectMapper.readValue(resultString, Sai2PayCheckPhResponse.class);
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_GATEWAY, "Payment server return unprocessable entity.");
        }

        if (!result.getRespCode().equals(ApiResponseStatus.SUCCESS.getValue())) {
            log.error("INVALID_UAB_RESPONSE >>> " + resultString);
            throw new GeneralException(HttpStatus.BAD_GATEWAY, result.getRespDescription());
        }

        // TODO: check hash value response
        String resultHash = securityManager.generateUABHashHmac(result.getSignatureString());
        if (!resultHash.equals(result.getHashValue())) {
            log.error("INVALID_UAB_RESPONSE >>>" + resultString);
            throw new GeneralException(HttpStatus.BAD_GATEWAY, "Invalid Server Response!");
        }

        return result;
    }

    public Sai2PayEnquiryPaymentResponse requestPayment(String invoiceNo, String sequenceNo, String amount,
                                                        String remark, String walletUserId, String tokenString) {
        Sai2PayEnquiryPaymentRequest request = new Sai2PayEnquiryPaymentRequest(uabProperties.getChannel(),
                uabProperties.getAppName(), uabProperties.getUser(), invoiceNo, sequenceNo, amount, remark,
                walletUserId, uabProperties.getEnquiryCallbackUrl(), 300, "");
        request.setHashValue(securityManager.generateUABHashHmac(request.getSignatureString()));
        String rawUrl = uabProperties.getEnquiryPaymentUrl();

        String resultString = "";
        Sai2PayEnquiryPaymentResponse result = new Sai2PayEnquiryPaymentResponse();
        try {
            resultString = httpRequestManager.jsonPostRequest(new URL(rawUrl), objectMapper.writeValueAsString(request),
                    tokenString, false);
            result = objectMapper.readValue(resultString, Sai2PayEnquiryPaymentResponse.class);
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_GATEWAY, "Payment server return unprocessable entity.");
        }

        if (!result.getRespCode().equals(ApiResponseStatus.SUCCESS.getValue())) {
            log.error("INVALID_UAB_RESPONSE >>> " + resultString);
            throw new GeneralException(HttpStatus.BAD_GATEWAY, result.getRespDescription());
        }

        // TODO: check hash value response
        String resultHash = securityManager.generateUABHashHmac(result.getSignatureString());
        if (!resultHash.equals(result.getHashValue())) {
            log.error("INVALID_UAB_RESPONSE >>>" + resultString);
            throw new GeneralException(HttpStatus.BAD_GATEWAY, "Invalid Server Response!");
        }

        return result;
    }

    private void writeLog(Sai2PayResponsePaymentRequest request) {
        try {
            log.error("INVALID_UAB_RESPONSE >>>" + objectMapper.writeValueAsString(request));
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_GATEWAY, "Payment server return unprocessable entity.");
        }
    }

    public Sai2PayResponsePaymentResponse enquiryPaymentCallback(Sai2PayResponsePaymentRequest request,
                                                                 List<Map<String, String>> itemList) {
        String requestHash = securityManager.generateUABHashHmac(request.getSignatureString());
        if (!requestHash.equals(request.getHashValue())) {
            writeLog(request);
            throw new GeneralException(HttpStatus.BAD_GATEWAY, "Invalid Server Response!");
        }

        if (!request.getTransactionStatus().equals(ApiResponseStatus.SUCCESS.getValue())) {
            writeLog(request);
            throw new GeneralException(HttpStatus.BAD_GATEWAY, "Invalid Server Response!");
        }

        // TODO: call repository with invoice no and update status
        // TODO: check response transaction status code "000" success
        String itemListStr = "";
        try {
            itemListStr = objectMapper.writeValueAsString(itemList);
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_GATEWAY, "Payment server return unprocessable entity.");
        }

        Sai2PayResponsePaymentResponse response = new Sai2PayResponsePaymentResponse(request.getReferIntegrationId(),
                Sai2PayResponsePaymentResponse.DataType.Data, "", itemListStr, "Success",
                TransactionStatus.SUCCESS.getValue(), "");

        response.setHashValue(securityManager.generateUABHashHmac(response.getSignatureString()));

        return response;
    }

    public Map<String, String> buildItem(String id, String qty, String price) {
        Map<String, String> item = new HashMap<>();
        item.put("ItemId", id);
        item.put("Quantity", qty);
        item.put("EachPrice", price);
        return item;
    }

    public Sai2PayCheckTransactionResponse checkTransactionStatus(String ReferIntegrationID, String tokenString) {
        Sai2PayCheckTransactionRequest request = new Sai2PayCheckTransactionRequest(uabProperties.getUser(),
                uabProperties.getChannel(), uabProperties.getAppName(), ReferIntegrationID, "");
        request.setHashValue(securityManager.generateUABHashHmac(request.getSignatureString()));
        String rawUrl = uabProperties.getCheckTransactionStatus();

        String resultString = "";
        Sai2PayCheckTransactionResponse result = new Sai2PayCheckTransactionResponse();
        try {
            resultString = httpRequestManager.jsonPostRequest(new URL(rawUrl), objectMapper.writeValueAsString(request),
                    tokenString, false);
            result = objectMapper.readValue(resultString, Sai2PayCheckTransactionResponse.class);
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_GATEWAY, "Payment server return unprocessable entity.");
        }

        if (!result.getRespCode().equals(ApiResponseStatus.SUCCESS.getValue())) {
            log.error("INVALID_UAB_RESPONSE >>> " + resultString);
            throw new GeneralException(HttpStatus.BAD_GATEWAY, result.getRespDescription());
        }

        // TODO: check hash value response
        String resultHash = securityManager.generateUABHashHmac(result.getSignatureString());
        if (!resultHash.equals(result.getHashValue())) {
            log.error("INVALID_UAB_RESPONSE >>>" + resultString);
            throw new GeneralException(HttpStatus.BAD_GATEWAY, "Invalid Server Response!");
        }

        return result;
    }

}
