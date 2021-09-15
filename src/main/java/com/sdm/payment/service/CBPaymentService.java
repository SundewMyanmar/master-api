package com.sdm.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.util.HttpRequestManager;
import com.sdm.core.util.LocaleManager;
import com.sdm.payment.config.properties.CBPayProperties;
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

import java.net.URL;

@Log4j2
@Service
public class CBPaymentService {
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

    public String getDeepLink(String id) {
        return cbProperties.getDeepLinkUrl(id);
    }

    public CBPaymentOrderResponse requestPayment(String orderId, String orderDetails, String amount) {
        CBPaymentOrderRequest request = new CBPaymentOrderRequest(cbProperties.getAuthToken(), cbProperties.getEcommerceId(), cbProperties.getSubMerId(), CBPaymentOrderRequest.TransactionType.WEB,
                orderId, orderDetails, amount, cbProperties.getCurrency(), cbProperties.getPaymentCallbackUrl(), "");
        try {
            request.setSignature(securityManager.generateCBHashSHA256(request.getSignatureString()));
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("payment-encryption-failed"));
        }

        String rawUrl = cbProperties.getPaymentOrderUrl();

        String resultString = "";
        CBPaymentOrderResponse result = new CBPaymentOrderResponse();
        try {
            resultString = httpRequestManager.jsonPostRequest(new URL(rawUrl), objectMapper.writeValueAsString(request), true);
            result = objectMapper.readValue(resultString, CBPaymentOrderResponse.class);
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("unprocessable-payment-response"));
        }

        result.setDeepLinkUrl(cbProperties.getDeepLinkUrl(result.getGenerateRefOrder()));

        if (!result.getCode().equals(ApiResponseStatus.SUCCESS.getValue())) {
            log.error("INVALID_CB_RESPONSE >>> " + resultString);
            throw new GeneralException(HttpStatus.BAD_REQUEST, result.getMsg());
        }

        return result;
    }

    public CBCheckPaymentStatusResponse checkPaymentStatus(String orderId, String generateRefOrder) {
        CBCheckPaymentStatusRequest request = new CBCheckPaymentStatusRequest(orderId, cbProperties.getEcommerceId(), generateRefOrder);
        String rawUrl = cbProperties.getCheckPaymentStatusUrl();

        try {
            String resultString = httpRequestManager.jsonPostRequest(new URL(rawUrl), objectMapper.writeValueAsString(request), true);
            return objectMapper.readValue(resultString, CBCheckPaymentStatusResponse.class);
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("unprocessable-payment-response"));
        }
    }

    public CBResponsePaymentOrderResponse paymentOrderNotifyCallback(CBResponsePaymentOrderRequest request, String orderId) {
        String requestHash = "";
        try {
            requestHash = securityManager.generateCBHashSHA256(
                    cbProperties.getAuthToken() + "&" + cbProperties.getEcommerceId() + "&" + cbProperties.getSubMerId() + "&" + orderId + "&" + request.getAmount() + "&" + request.getCurrency()
            );
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("payment-encryption-failed"));
        }


        if (!requestHash.equals(request.getSignature())) {
            try {
                log.error("INVALID_CB_RESPONSE >>>" + objectMapper.writeValueAsString(request));
            } catch (Exception ex) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("unprocessable-payment-response"));
            }

            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-payment-server-response"));
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
            try {
                log.error("INVALID_CB_RESPONSE >>>" + objectMapper.writeValueAsString(request));
            } catch (Exception ex) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("unprocessable-payment-response"));
            }
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("unprocessable-payment-response"));
        }

        CBResponsePaymentOrderResponse response = new CBResponsePaymentOrderResponse("0000", "OPERATION SUCCESS");
        return response;
    }
}
