package com.sdm.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.payment.config.properties.PaymentProperties;
import com.sdm.payment.config.properties.YOMAProperties;
import com.sdm.payment.model.request.YOMAPaymentRequest;
import com.sdm.payment.model.request.YOMAResponsePaymentRequest;
import com.sdm.payment.model.response.YOMAPaymentResponse;
import com.sdm.payment.util.PaymentSecurityManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class YOMAPaymentService {
    @Autowired
    PaymentSecurityManager securityManager;

    @Autowired
    PaymentService paymentService;

    @Autowired
    private YOMAProperties yomaProperties;

    @Autowired
    private PaymentProperties paymentProperties;

    private Map<String,String> getRequestItems(String name,String amount){
        Map<String,String> item=new HashMap<>();
        item.put("name",name);
        item.put("amount",amount);
        return item;
    }

    public YOMAPaymentResponse requestPayment(String orderId, String merchantReferenceId,String amount, String paymentDescription,String items) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        ObjectMapper mapper=new ObjectMapper();

        List<Map<String,String>> itemList=new ArrayList<>();
        itemList.add(this.getRequestItems("ITEM 1","1000"));
        itemList.add(this.getRequestItems("ITEM 2","1000"));
        items=mapper.writeValueAsString(itemList);

        YOMAPaymentRequest request=new YOMAPaymentRequest(yomaProperties.getMerchantId(),orderId,merchantReferenceId,yomaProperties.getFrontEndResultUrl(),yomaProperties.getPaymentCallbackUrl()
                ,amount,yomaProperties.getTimeToLiveInSeconds().toString(),paymentDescription,yomaProperties.getMerchantName(),items,"");

        String requestHash=securityManager.generateYOMAHashSHA256(request.getSignatureString());
        request.setHash(requestHash);
        String rawUrl=yomaProperties.getPaymentRequestUrl();
        String resultString=paymentService.requestApi_POST_SSL(new URL(rawUrl),mapper.writeValueAsString(request),null);

        YOMAPaymentResponse result=mapper.readValue(resultString,YOMAPaymentResponse.class);
        result.setAuthenticatedUrl(yomaProperties.getPaymentRequestAuthenticateUrl(result.getTransactionId()));

        if(!result.getMessage().equals("success")){
            log.error("INVALID_YOMA_RESPONSE>>>"+resultString);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR,result.getMessage());
        }
//        if(!result.getStatusCode().equals(YOMAProperties.APIPaymentResponseStatus.SUCCESS.getValue())){
//            log.error("INVALID_YOMA_RESPONSE>>>"+resultString);
//            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR,result.getMessage());
//        }
        return result;
    }

    public ResponseEntity requestPaymentCallback(String request){
        return null;
    }

    public ResponseEntity requestPaymentCallback(YOMAResponsePaymentRequest request) throws JsonProcessingException {
        ObjectMapper mapper=new ObjectMapper();
        String requestHash=securityManager.generateYOMAHashSHA256(request.getSignatureString());
        if(!requestHash.equals(request.getHashValue())){
            log.error("INVALID_YOMA_RESPONSE >>>"+mapper.writeValueAsString(request));
            throw new GeneralException(HttpStatus.NON_AUTHORITATIVE_INFORMATION, "Invalid Server Response!");
        }

        return ResponseEntity.ok(new MessageResponse("SUCCESS","YOMA Callback is success for transactionId : "+request.getTransactionId()));
    }
}
