package com.sdm.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.exception.GeneralException;
import com.sdm.payment.config.properties.AGDProperties;
import com.sdm.payment.config.properties.PaymentProperties;
import com.sdm.payment.model.request.AGDCheckTransactionRequest;
import com.sdm.payment.model.request.AGDDirectPaymentRequest;
import com.sdm.payment.model.request.AGDResponseDirectPaymentRequest;
import com.sdm.payment.model.request.AGDVerifyPhRequest;
import com.sdm.payment.model.response.AGDCheckTransactionResponse;
import com.sdm.payment.model.response.AGDDirectPaymentResponse;
import com.sdm.payment.model.response.AGDResponseDirectPaymentResponse;
import com.sdm.payment.model.response.AGDVerifyPhResponse;
import com.sdm.payment.util.PaymentSecurityManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class AGDPaymentService {
    @Autowired
    PaymentSecurityManager securityManager;

    @Autowired
    PaymentService paymentService;

    @Autowired
    private AGDProperties agdProperties;

    @Autowired
    private PaymentProperties paymentProperties;

    public AGDVerifyPhResponse verifyPhone(String phoneNo)throws IOException{
        ObjectMapper mapper=new ObjectMapper();
        AGDVerifyPhRequest request=new AGDVerifyPhRequest(agdProperties.getChannel(),agdProperties.getUser(),phoneNo,"");
        request.setHashValue(securityManager.generateAGDHashHMac(request.getSignatureString()));
        String rawUrl=agdProperties.getVerifyPhoneUrl();

        String resultString=paymentService.requestApi_POST(new URL(rawUrl),mapper.writeValueAsString(request),null);
        AGDVerifyPhResponse result=mapper.readValue(resultString,AGDVerifyPhResponse.class);
        if(!result.getRespCode().equals(AGDProperties.APIResponseStatus.SUCCESS.getValue())){
            log.error("INVALID_AGD_RESPONSE >>> "+resultString);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, result.getRespDescription());
        }

        //TODO: check hash value response
        String resultHash=securityManager.generateAGDHashHMac(result.getSignatureString());
        if(!resultHash.equals(result.getHashValue())){
            log.error("INVALID_AGD_RESPONSE >>>"+resultString);
            throw new GeneralException(HttpStatus.NON_AUTHORITATIVE_INFORMATION, "Invalid Server Response!");
        }

        return result;
    }

    public AGDDirectPaymentResponse directPayment(String invoiceNo, String sequenceNo,String amount,String remark,String walletUserId)throws  IOException{
        ObjectMapper mapper=new ObjectMapper();
        AGDDirectPaymentRequest request=new AGDDirectPaymentRequest(agdProperties.getVersion(),agdProperties.getChannel(),agdProperties.getUser(),invoiceNo,sequenceNo,amount,remark,walletUserId,agdProperties.getDirectPaymentCallbackUrl(),paymentProperties.getExpiredSeconds(),"");
        request.setHashValue(securityManager.generateAGDHashHMac(request.getSignatureString()));
        String rawUrl=agdProperties.getDirectPaymentUrl();

        String resultString=paymentService.requestApi_POST(new URL(rawUrl),mapper.writeValueAsString(request),null);
        AGDDirectPaymentResponse result=mapper.readValue(resultString,AGDDirectPaymentResponse.class);
        if(!result.getRespCode().equals(AGDProperties.APIResponseStatus.SUCCESS.getValue())){
            log.error("INVALID_AGD_RESPONSE >>> "+resultString);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, result.getRespDescription());
        }

        //TODO: check hash value response
        String resultHash=securityManager.generateAGDHashHMac(result.getSignatureString());
        if(!resultHash.equals(result.getHashValue())){
            log.error("INVALID_AGD_RESPONSE >>>"+resultString);
            throw new GeneralException(HttpStatus.NON_AUTHORITATIVE_INFORMATION, "Invalid Server Response!");
        }

        return result;
    }

    private Map<String,String> getResponseItemData(String id,String qty,String price){
        Map<String,String> item=new HashMap<>();
        item.put("ItemId",id);
        item.put("Quantity",qty);
        item.put("EachPrice",price);
        return item;
    }

    public AGDResponseDirectPaymentResponse directPaymentCallback(AGDResponseDirectPaymentRequest request)throws IOException{
        ObjectMapper mapper=new ObjectMapper();
        String requestHash=securityManager.generateAGDHashHMac(request.getSignatureString());
        if(!requestHash.equals(request.getHashValue())){
            log.error("INVALID_AGD_RESPONSE >>>"+mapper.writeValueAsString(request));
            throw new GeneralException(HttpStatus.NON_AUTHORITATIVE_INFORMATION, "Invalid Server Response!");
        }

        String invoiceNo=request.getInvoiceNo();
        //TODO: call repository with invoice no and update status
        //TODO: check response transaction status code "000" success
        List<Map<String,String>> itemList=new ArrayList<>();
        itemList.add(this.getResponseItemData("ITEM 1","1","1"));
        itemList.add(this.getResponseItemData("ITEM 2","1","1"));
        String itemListStr=mapper.writeValueAsString(itemList);
        itemListStr=itemListStr.replace("[","");
        itemListStr=itemListStr.replace("]","");
        AGDResponseDirectPaymentResponse response=new AGDResponseDirectPaymentResponse(request.getReferIntegrationId(),AGDResponseDirectPaymentResponse.DataType.Data,"",itemListStr,"Success",AGDProperties.TransactionStatus.SUCCESS.getValue(),"");
        response.setHashValue(securityManager.generateAGDHashHMac(response.getSignatureString()));

        return response;
    }

    public AGDCheckTransactionResponse checkTransactionStatus(String ReferIntegrationId)throws IOException{
        ObjectMapper mapper=new ObjectMapper();
        AGDCheckTransactionRequest request=new AGDCheckTransactionRequest(agdProperties.getUser(),ReferIntegrationId,"");
        request.setHashValue(securityManager.generateAGDHashHMac(request.getSignatureString()));
        String rawUrl=agdProperties.getCheckTransactionUrl();
        String resultString=paymentService.requestApi_POST(new URL(rawUrl),mapper.writeValueAsString(request),null);
        AGDCheckTransactionResponse result=mapper.readValue(resultString,AGDCheckTransactionResponse.class);
        if(!result.getRespCode().equals(AGDProperties.APIResponseStatus.SUCCESS.getValue())){
            log.error("INVALID_AGD_RESPONSE >>> "+resultString);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, result.getRespDescription());
        }

        //TODO: check hash value response
        String resultHash=securityManager.generateAGDHashHMac(result.getSignatureString());
        if(!resultHash.equals(result.getHashValue())){
            log.error("INVALID_AGD_RESPONSE >>>"+resultString);
            throw new GeneralException(HttpStatus.NON_AUTHORITATIVE_INFORMATION, "Invalid Server Response!");
        }

        return result;
    }
}
