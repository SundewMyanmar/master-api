package com.sdm.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.HttpResponse;
import com.sdm.core.util.HttpRequestManager;
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
import com.sdm.payment.util.PaymentSecurityManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

@Log4j2
@Service
public class CBPaymentService extends BasePaymentService {
    @Override
    public String getPayment() {
        return "CB_PAY";
    }

    /**
     * CBPAY
     * TODO: TRANSACTION CANCEL,TIMEOUT, SYSTEM_ERROR
     * <p>
     * Payment expired 5 min
     * callback က real-time တစ်ကြိမ်ပဲ ခေါ်ပါတယ် အစ်ကို ... timeout ဖြစ်သွားခဲ့ရင်တော့ user ကို merchant app ဘက်မှာ order ပြန်ကြည့်တဲ့နေရာမျိုးမှာ e.g. "update order status" button နှိပ်ခိုင်းပြီး Check Payment Status ကို ခေါ်ပေးရပါမယ်
     */
    @Autowired
    private PaymentSecurityManager securityManager;

    @Autowired
    private HttpRequestManager httpRequestManager;

    @Autowired
    private CBPayProperties cbProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LocaleManager localeManager;

    public CBPaymentOrderResponse paymentRequest(String orderId, String orderDetails, String amount) {
        try {
            CBPaymentOrderRequest request = new CBPaymentOrderRequest(cbProperties.getAuthToken(), cbProperties.getEcommerceId(),
                    cbProperties.getSubMerId(), CBPaymentOrderRequest.TransactionType.MOBILE,
                    orderId, orderDetails, amount, cbProperties.getCurrency(), cbProperties.getPaymentCallbackUrl(), "");

            request.setSignature(securityManager.generateCBHashSHA256(request.getSignatureString()));

            String rawUrl = cbProperties.getPaymentOrderUrl();
            String requestString = objectMapper.writeValueAsString(request);
            writeLog(LogType.REQUEST, requestString);

            HttpResponse serverResponse = httpRequestManager.jsonPostRequest(new URL(rawUrl), requestString, true);
            writeLog(LogType.RESPONSE, serverResponse.getBody());
            CBPaymentOrderResponse response = objectMapper.readValue(serverResponse.getBody(), CBPaymentOrderResponse.class);

            response.setDeepLinkUrl(cbProperties.getDeepLinkUrl(response.getGenerateRefOrder()));

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
            CBCheckPaymentStatusRequest request = new CBCheckPaymentStatusRequest(orderId, cbProperties.getEcommerceId(), generateRefOrder);
            String rawUrl = cbProperties.getCheckPaymentStatusUrl();
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
            String requestHash = securityManager.generateCBHashSHA256(
                    cbProperties.getAuthToken() + "&" + cbProperties.getEcommerceId() + "&" + cbProperties.getSubMerId() + "&" + orderId + "&" + request.getAmount() + "&" + request.getCurrency()
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
