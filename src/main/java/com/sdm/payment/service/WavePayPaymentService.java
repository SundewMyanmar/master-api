package com.sdm.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.payment.config.properties.PaymentProperties;
import com.sdm.payment.config.properties.YOMAProperties;
import com.sdm.payment.model.request.wavepay.WavePayPaymentRequest;
import com.sdm.payment.model.request.wavepay.WavePayResponsePaymentRequest;
import com.sdm.payment.model.response.wavepay.WavePayPaymentResponse;
import com.sdm.payment.util.PaymentSecurityManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
public class WavePayPaymentService {
    @Autowired
    private PaymentSecurityManager securityManager;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private YOMAProperties yomaProperties;

    @Autowired
    private ObjectMapper objectMapper;
    

    private Map<String, String> buildItem(String name, String amount) {
        Map<String, String> item = new HashMap<>();
        item.put("name", name);
        item.put("amount", amount);
        return item;
    }

    public WavePayPaymentResponse requestPayment(String orderId, String merchantReferenceId, String amount, String paymentDescription, String items) {
        WavePayPaymentRequest request = new WavePayPaymentRequest(yomaProperties.getMerchantId(), orderId, merchantReferenceId, yomaProperties.getFrontEndResultUrl(), yomaProperties.getPaymentCallbackUrl()
                , amount, yomaProperties.getTimeToLiveInSeconds().toString(), paymentDescription, yomaProperties.getMerchantName(), items, "");

        String requestHash = securityManager.generateYOMAHashSHA256(request.getSignatureString());
        request.setHash(requestHash);
        String rawUrl = yomaProperties.getPaymentRequestUrl();

        String resultString = "";
        WavePayPaymentResponse result = new WavePayPaymentResponse();
        try {
            resultString = paymentService.requestApi_POST_SSL(new URL(rawUrl), objectMapper.writeValueAsString(request), null);

            result = objectMapper.readValue(resultString, WavePayPaymentResponse.class);
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_GATEWAY, "Payment server return unprocessable entity.");
        }

        result.setAuthenticatedUrl(yomaProperties.getPaymentRequestAuthenticateUrl(result.getTransactionId()));

        if (!result.getMessage().equals("success")) {
            log.error("INVALID_YOMA_RESPONSE>>>" + resultString);
            throw new GeneralException(HttpStatus.BAD_GATEWAY, result.getMessage());
        }
//        if(!result.getStatusCode().equals(YOMAProperties.APIPaymentResponseStatus.SUCCESS.getValue())){
//            log.error("INVALID_YOMA_RESPONSE>>>"+resultString);
//            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR,result.getMessage());
//        }
        return result;
    }

    public ResponseEntity<MessageResponse> requestPaymentCallback(WavePayResponsePaymentRequest request) {
        String requestHash = securityManager.generateYOMAHashSHA256(request.getSignatureString());
        if (!requestHash.equals(request.getHashValue())) {
            try {
                log.error("INVALID_YOMA_RESPONSE >>>" + objectMapper.writeValueAsString(request));
            } catch (Exception ex) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, "Payment encryption fail.");
            }

            throw new GeneralException(HttpStatus.BAD_GATEWAY, "Invalid Server Response!");
        }

        return ResponseEntity.ok(new MessageResponse("SUCCESS", "YOMA Callback is success for transactionId : " + request.getTransactionId()));
    }
}
