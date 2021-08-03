package com.sdm.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.HttpRequestManager;
import com.sdm.core.util.LocaleManager;
import com.sdm.payment.config.properties.OnePayProperties;
import com.sdm.payment.model.request.onepay.OnePayCheckTransactionRequest;
import com.sdm.payment.model.request.onepay.OnePayDirectPaymentRequest;
import com.sdm.payment.model.request.onepay.OnePayResponseDirectPaymentRequest;
import com.sdm.payment.model.request.onepay.OnePayVerifyPhRequest;
import com.sdm.payment.model.response.onepay.*;
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
public class OnePayPaymentService {
    @Autowired
    private PaymentSecurityManager securityManager;

    @Autowired
    private HttpRequestManager httpRequestManager;

    @Autowired
    private OnePayProperties agdProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LocaleManager localeManager;

    public OnePayVerifyPhResponse verifyPhone(String phoneNo) {
        OnePayVerifyPhRequest request = new OnePayVerifyPhRequest(agdProperties.getChannel(), agdProperties.getUser(),
                agdProperties.getPhoneNo(Globalizer.cleanPhoneNo(phoneNo)),
                "");
        request.setHashValue(securityManager.generateAGDHashHMac(request.getSignatureString()));
        String rawUrl = agdProperties.getVerifyPhoneUrl();

        String resultString = "";
        OnePayVerifyPhResponse result = new OnePayVerifyPhResponse();
        try {
            resultString = httpRequestManager.jsonPostRequest(new URL(rawUrl), objectMapper.writeValueAsString(request), false);
            result = objectMapper.readValue(resultString, OnePayVerifyPhResponse.class);
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_GATEWAY, localeManager.getMessage("unprocessable-payment-response"));
        }

        if (!result.getRespCode().equals(ApiResponseStatus.SUCCESS.getValue())) {
            log.error("INVALID_AGD_RESPONSE >>> " + resultString);
            throw new GeneralException(HttpStatus.BAD_GATEWAY, result.getRespDescription());
        }

        //TODO: check hash value response
        String resultHash = securityManager.generateAGDHashHMac(result.getSignatureString());
        if (!resultHash.equals(result.getHashValue())) {
            log.error("INVALID_AGD_RESPONSE >>>" + resultString);
            throw new GeneralException(HttpStatus.BAD_GATEWAY, localeManager.getMessage("invalid-payment-server-response"));
        }

        return result;
    }

    public OnePayDirectPaymentResponse requestPayment(String invoiceNo, String sequenceNo, String amount, String remark, String walletUserId) {
        OnePayDirectPaymentRequest request = new OnePayDirectPaymentRequest(agdProperties.getVersion(), agdProperties.getChannel(),
                agdProperties.getUser(), invoiceNo, sequenceNo, amount, remark,
                agdProperties.getPhoneNo(Globalizer.cleanPhoneNo(walletUserId)),
                agdProperties.getPaymentCallbackUrl(),
                300, "");
        request.setHashValue(securityManager.generateAGDHashHMac(request.getSignatureString()));
        String rawUrl = agdProperties.getDirectPaymentUrl();

        String resultString = "";
        OnePayDirectPaymentResponse result = new OnePayDirectPaymentResponse();
        try {
            resultString = httpRequestManager.jsonPostRequest(new URL(rawUrl), objectMapper.writeValueAsString(request), false);
            result = objectMapper.readValue(resultString, OnePayDirectPaymentResponse.class);
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_GATEWAY, localeManager.getMessage("unprocessable-payment-response"));
        }

        if (!result.getRespCode().equals(ApiResponseStatus.SUCCESS.getValue())) {
            log.error("INVALID_AGD_RESPONSE >>> " + resultString);
            throw new GeneralException(HttpStatus.BAD_GATEWAY, result.getRespDescription());
        }

        //TODO: check hash value response
        String resultHash = securityManager.generateAGDHashHMac(result.getSignatureString());
        if (!resultHash.equals(result.getHashValue())) {
            log.error("INVALID_AGD_RESPONSE >>>" + resultString);
            throw new GeneralException(HttpStatus.BAD_GATEWAY, localeManager.getMessage("invalid-payment-server-response"));
        }

        return result;
    }

    public Map<String, String> buildItem(String id, String qty, String price) {
        Map<String, String> item = new HashMap<>();
        item.put("ItemId", id);
        item.put("Quantity", qty);
        item.put("EachPrice", price);
        return item;
    }

    private void writeLog(OnePayResponseDirectPaymentRequest request) {
        try {
            log.error("INVALID_AGD_RESPONSE >>>" + objectMapper.writeValueAsString(request));
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_GATEWAY, localeManager.getMessage("unprocessable-payment-response"));
        }
    }

    public OnePayResponseDirectPaymentResponse directPaymentCallback(OnePayResponseDirectPaymentRequest request, List<Map<String, String>> itemList) {
        String requestHash = securityManager.generateAGDHashHMac(request.getSignatureString());
        if (!requestHash.equals(request.getHashValue())) {
            writeLog((request));
            throw new GeneralException(HttpStatus.BAD_GATEWAY, localeManager.getMessage("invalid-payment-server-response"));
        }

        if (!request.getTransactionStatus().equals(ApiResponseStatus.SUCCESS.getValue())) {
            writeLog((request));
            throw new GeneralException(HttpStatus.BAD_GATEWAY, localeManager.getMessage("invalid-payment-server-response"));
        }

        //TODO: call repository with invoice no and update status
        //TODO: check response transaction status code "000" success
        String itemListStr = "";
        try {
            itemListStr = objectMapper.writeValueAsString(itemList);
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_GATEWAY, localeManager.getMessage("unprocessable-payment-response"));
        }

        itemListStr = itemListStr.replace("[", "");
        itemListStr = itemListStr.replace("]", "");
        OnePayResponseDirectPaymentResponse response = new OnePayResponseDirectPaymentResponse(request.getReferIntegrationId(), OnePayResponseDirectPaymentResponse.DataType.Data, "", itemListStr, "Success", TransactionStatus.SUCCESS.getValue(), "");
        response.setHashValue(securityManager.generateAGDHashHMac(response.getSignatureString()));

        return response;
    }

    public OnePayCheckTransactionResponse checkTransactionStatus(String ReferIntegrationId) {
        OnePayCheckTransactionRequest request = new OnePayCheckTransactionRequest(agdProperties.getUser(), ReferIntegrationId, "");
        request.setHashValue(securityManager.generateAGDHashHMac(request.getSignatureString()));
        String rawUrl = agdProperties.getCheckTransactionUrl();

        String resultString = "";
        OnePayCheckTransactionResponse result = new OnePayCheckTransactionResponse();
        try {
            resultString = httpRequestManager.jsonPostRequest(new URL(rawUrl), objectMapper.writeValueAsString(request), false);
            result = objectMapper.readValue(resultString, OnePayCheckTransactionResponse.class);
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_GATEWAY, localeManager.getMessage("unprocessable-payment-response"));
        }

        if (!result.getRespCode().equals(ApiResponseStatus.SUCCESS.getValue())) {
            log.error("INVALID_AGD_RESPONSE >>> " + resultString);
            throw new GeneralException(HttpStatus.BAD_GATEWAY, result.getRespDescription());
        }

        //TODO: check hash value response
        String resultHash = securityManager.generateAGDHashHMac(result.getSignatureString());
        if (!resultHash.equals(result.getHashValue())) {
            log.error("INVALID_AGD_RESPONSE >>>" + resultString);
            throw new GeneralException(HttpStatus.BAD_GATEWAY, localeManager.getMessage("invalid-payment-server-response"));
        }

        return result;
    }
}
