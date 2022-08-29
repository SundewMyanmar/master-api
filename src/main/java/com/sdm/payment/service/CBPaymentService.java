package com.sdm.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.HttpResponse;
import com.sdm.core.util.HttpRequestManager;
import com.sdm.core.util.ISettingManager;
import com.sdm.core.util.LocaleManager;
import com.sdm.payment.config.properties.CBPayProperties;
import com.sdm.payment.exception.CallbackException;
import com.sdm.payment.exception.FailedType;
import com.sdm.payment.model.request.cbpay.CBCheckPaymentStatusRequest;
import com.sdm.payment.model.request.cbpay.CBPaymentOrderRequest;
import com.sdm.payment.model.request.cbpay.CBResponsePaymentOrderRequest;
import com.sdm.payment.model.response.cbpay.ApiResponseStatus;
import com.sdm.payment.model.response.cbpay.CBCheckPaymentStatusResponse;
import com.sdm.payment.model.response.cbpay.CBPaymentOrderResponse;
import com.sdm.payment.model.response.cbpay.CBResponsePaymentOrderResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
/**
 * CBPAY
 * TODO: TRANSACTION CANCEL,TIMEOUT, SYSTEM_ERROR
 * <p>
 * Payment expired 5 min
 * callback က real-time တစ်ကြိမ်ပဲ ခေါ်ပါတယ် အစ်ကို ... timeout ဖြစ်သွားခဲ့ရင်တော့ user ကို merchant app ဘက်မှာ order ပြန်ကြည့်တဲ့နေရာမျိုးမှာ e.g. "update order status" button နှိပ်ခိုင်းပြီး Check Payment Status ကို ခေါ်ပေးရပါမယ်
 */
public class CBPaymentService extends BasePaymentService {
    @Override
    public String getPayment() {
        return "CB_PAY";
    }

    @Autowired
    private HttpRequestManager httpRequestManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LocaleManager localeManager;

    @Autowired
    private ISettingManager settingManager;

    private CBPayProperties getProperties() {
        CBPayProperties properties = new CBPayProperties();
        try {
            properties = settingManager.loadSetting(CBPayProperties.class);
        } catch (IOException | IllegalAccessException ex) {
            log.error(ex.getLocalizedMessage());
        }
        return properties;
    }

    public String generateHash(String stringData) throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        final byte[] hashbytes = digest.digest(
                stringData.getBytes(StandardCharsets.UTF_8));
        return DatatypeConverter.printHexBinary(hashbytes).toLowerCase();
    }

    public CBPaymentOrderResponse paymentRequest(String orderId, String orderDetails, String amount) {
        try {
            CBPaymentOrderRequest request = new CBPaymentOrderRequest(this.getProperties().getAuthToken(), this.getProperties().getEcommerceId(),
                    this.getProperties().getSubMerId(), CBPaymentOrderRequest.TransactionType.MOBILE,
                    orderId, orderDetails, amount, this.getProperties().getCurrency(), this.getProperties().getPaymentCallbackUrl(), "");

            request.setSignature(generateHash(request.getSignatureString()));

            String rawUrl = this.getProperties().getPaymentOrderUrl();
            String requestString = objectMapper.writeValueAsString(request);
            writeLog(LogType.REQUEST, requestString);

            HttpResponse serverResponse = httpRequestManager.jsonPostRequest(new URL(rawUrl), requestString, true);
            writeLog(LogType.RESPONSE, serverResponse.getBody());
            CBPaymentOrderResponse response = objectMapper.readValue(serverResponse.getBody(), CBPaymentOrderResponse.class);

            response.setDeepLinkUrl(this.getProperties().getDeepLinkUrl(response.getGenerateRefOrder()));

            if (!response.getCode().equals(ApiResponseStatus.SUCCESS.getValue())) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + response.getMsg());
            }

            return response;
        } catch (NoSuchAlgorithmException | JsonProcessingException | MalformedURLException ex) {
            writeLog(LogType.ERROR, ex.getLocalizedMessage());
            throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + ex.getLocalizedMessage());
        }
    }

    public CBCheckPaymentStatusResponse checkStatus(String orderId, String generateRefOrder) {
        try {
            CBCheckPaymentStatusRequest request = new CBCheckPaymentStatusRequest(orderId, this.getProperties().getEcommerceId(), generateRefOrder);
            String rawUrl = this.getProperties().getCheckPaymentStatusUrl();
            String requestString = objectMapper.writeValueAsString(request);
            writeLog(LogType.REQUEST, requestString);

            HttpResponse serverResponse = httpRequestManager.jsonPostRequest(new URL(rawUrl), requestString, true);
            writeLog(LogType.RESPONSE, serverResponse.getBody());
            return objectMapper.readValue(serverResponse.getBody(), CBCheckPaymentStatusResponse.class);
        } catch (JsonProcessingException | MalformedURLException ex) {
            writeLog(LogType.ERROR, ex.getLocalizedMessage());
            throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + ex.getLocalizedMessage());
        }
    }

    public CBResponsePaymentOrderResponse paymentCallback(CBResponsePaymentOrderRequest request, String orderId) throws CallbackException {
        try {
            writeLog(LogType.CALLBACK_REQUEST, objectMapper.writeValueAsString(request));
            String requestHash = generateHash(
                    this.getProperties().getAuthToken() + "&" +
                            this.getProperties().getEcommerceId() + "&" +
                            this.getProperties().getSubMerId() + "&" +
                            orderId + "&" + request.getAmount() + "&" + request.getCurrency()
            );
            if (!requestHash.equals(request.getSignature())) {
                throw new CallbackException(FailedType.INVALID_HASH, localeManager.getMessage("invalid-hash-value", getPayment()));
            }
            //TODO: check response status code "0000" success
            //TODO: call repository with invoice and update status

            /*
            P = pending
            S = Success
            F = Fail
            E = Expired
            * */
            if (!request.getTransactionStatus().equals("S")) {
                throw new CallbackException(FailedType.TRANSACTION_FAILED, localeManager.getMessage("transaction-failed", getPayment()));
            }

            CBResponsePaymentOrderResponse response = new CBResponsePaymentOrderResponse("0000", "Operation Success");
            writeLog(LogType.CALLBACK_RESPONSE, objectMapper.writeValueAsString(response));

            return response;
        } catch (JsonProcessingException | NoSuchAlgorithmException ex) {
            writeLog(LogType.ERROR, ex.getLocalizedMessage());
            throw new CallbackException(FailedType.RUNTIME_ERROR, ex.getLocalizedMessage(), ex);
        }
    }
}
