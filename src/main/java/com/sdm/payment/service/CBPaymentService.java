package com.sdm.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.exception.GeneralException;
import com.sdm.payment.config.properties.CBProperties;
import com.sdm.payment.config.properties.PaymentProperties;
import com.sdm.payment.model.request.CBCheckPaymentStatusRequest;
import com.sdm.payment.model.request.CBPaymentOrderRequest;
import com.sdm.payment.model.request.CBResponsePaymentOrderRequest;
import com.sdm.payment.model.response.CBCheckPaymentStatusResponse;
import com.sdm.payment.model.response.CBPaymentOrderResponse;
import com.sdm.payment.model.response.CBResponsePaymentOrderResponse;
import com.sdm.payment.util.PaymentSecurityManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Log4j2
@Service
public class CBPaymentService {
    @Autowired
    PaymentSecurityManager securityManager;

    @Autowired
    PaymentService paymentService;

    @Autowired
    CBProperties cbProperties;

    @Autowired
    private PaymentProperties paymentProperties;

    public String getDeepLink(String id){
        return cbProperties.getDeepLinkUrl(id);
    }

    public CBPaymentOrderResponse paymentOrder(String orderId,String orderDetails, String amount) throws NoSuchAlgorithmException, IOException, KeyManagementException {
        ObjectMapper mapper=new ObjectMapper();
        CBPaymentOrderRequest request=new CBPaymentOrderRequest(cbProperties.getAuthToken(),cbProperties.getEcommerceId(),cbProperties.getSubMerId(), CBPaymentOrderRequest.TransactionType.WEB,
                orderId,orderDetails,amount,"MMK",cbProperties.getNotifyUrl(),"");
        request.setSignature(securityManager.generateCBHashSHA256(request.getSignatureString()));
        String rawUrl=cbProperties.getPaymentOrderUrl();

        String resultString=paymentService.requestApi_POST_SSL(new URL(rawUrl),mapper.writeValueAsString(request),null);
        CBPaymentOrderResponse result=mapper.readValue(resultString,CBPaymentOrderResponse.class);
        result.setDeepLinkUrl(cbProperties.getDeepLinkUrl(result.getGenerateRefOrder()));

        if(!result.getCode().equals(CBProperties.APIPaymentResponseStatus.SUCCESS.getValue())){
            log.error("INVALID_CB_RESPONSE >>> "+resultString);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, result.getMsg());
        }

        return result;
    }

    public CBCheckPaymentStatusResponse checkPaymentStatus(String orderId, String generateRefOrder) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        ObjectMapper mapper=new ObjectMapper();
        CBCheckPaymentStatusRequest request=new CBCheckPaymentStatusRequest(orderId,cbProperties.getEcommerceId(),generateRefOrder);
        String rawUrl=cbProperties.getCheckPaymentStatusUrl();

        String resultString=paymentService.requestApi_POST_SSL(new URL(rawUrl),mapper.writeValueAsString(request),null);
        return mapper.readValue(resultString,CBCheckPaymentStatusResponse.class);
    }

    public CBResponsePaymentOrderResponse paymentOrderNotifyCallback(CBResponsePaymentOrderRequest request, String orderId) throws NoSuchAlgorithmException, JsonProcessingException {
        ObjectMapper mapper=new ObjectMapper();
        String requestHash=securityManager.generateCBHashSHA256(
                cbProperties.getAuthToken()+"&"+cbProperties.getEcommerceId()+"&"+cbProperties.getSubMerId()+"&"+orderId+"&"+request.getAmount()+"&"+request.getCurrency()
        );

        if(!requestHash.equals(request.getSignature())){
            log.error("INVALID_CB_RESPONSE >>>"+mapper.writeValueAsString(request));
            throw new GeneralException(HttpStatus.NON_AUTHORITATIVE_INFORMATION, "Invalid Server Response!");
        }
        //TODO: check response status code "0000" success
        //TODO: call repository with invoice and update status

        CBResponsePaymentOrderResponse response=new CBResponsePaymentOrderResponse("0000","OPERATION SUCCESS");
        return response;
    }
}
