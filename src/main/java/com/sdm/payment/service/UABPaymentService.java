package com.sdm.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.exception.GeneralException;
import com.sdm.payment.config.properties.PaymentProperties;
import com.sdm.payment.config.properties.UABProperties;
import com.sdm.payment.model.request.*;
import com.sdm.payment.model.response.*;
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
public class UABPaymentService {
    @Autowired
    PaymentSecurityManager securityManager;

    @Autowired
    private UABProperties uabProperties;

    @Autowired
    private PaymentProperties paymentProperties;

    @Autowired
    private PaymentService paymentService;

    public static void main(String[] args)throws IOException{
        ObjectMapper mapper=new ObjectMapper();
        UABLoginRequest request=new UABLoginRequest("info@sundewmyanmar.com","sundew");
        String result=mapper.writeValueAsString(request);
        Map<String,String> item=new HashMap<>();
        item.put("ItemId","1");
        item.put("Quantity","1");
        item.put("EachPrice","1");
        List<Map<String,String>> arr=new ArrayList<>();
        arr.add(item);
        String arrString=mapper.writeValueAsString(arr);
        if(result==null){

        }
    }

    public UABLoginResponse login() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        UABLoginRequest request=new UABLoginRequest(uabProperties.getUser(),uabProperties.getPassword());
        String rawUrl=uabProperties.getLoginUrl();

        String resultString=paymentService.requestApi_POST(new URL(rawUrl),mapper.writeValueAsString(request),null);
        UABLoginResponse result= mapper.readValue(resultString,UABLoginResponse.class);

        if(!result.getRespCode().equals(UABProperties.APIResponseStatus.SUCCESS.getValue())){
            log.error(resultString);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, result.getRespDescription());
        }

        return result;
    }

    //DANGER app.properties need to change if you call this api
    public UABLoginResponse changePassword(String oldPassword, String newPassword) throws IOException{
        if(!oldPassword.equals(uabProperties.getPassword())){
            log.error("Invalid Old Password : "+oldPassword);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid Old Password!");
        }

        ObjectMapper mapper = new ObjectMapper();
        UABChangePasswordRequest request=new UABChangePasswordRequest(uabProperties.getUser(),uabProperties.getPassword(),newPassword);
        String rawUrl=uabProperties.getChangePasswordUrl();

        String resultString=paymentService.requestApi_POST(new URL(rawUrl),mapper.writeValueAsString(request),null);
        UABLoginResponse result= mapper.readValue(resultString,UABLoginResponse.class);

        if(!result.getRespCode().equals(UABProperties.APIResponseStatus.SUCCESS.getValue())){
            log.error(resultString);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, result.getRespDescription());
        }

        return result;
    }

    public UABCheckPhResponse checkPhone(String phoneNo, String tokenString)throws IOException{
        ObjectMapper mapper=new ObjectMapper();
        UABCheckPhRequest request=new UABCheckPhRequest(uabProperties.getChannel(), uabProperties.getUser(),phoneNo,uabProperties.getAppName(),"");
        request.setHashValue(securityManager.generateUABHashHmac(request.getSignatureString()));
        String rawUrl=uabProperties.getCheckPhoneUrl();

        String resultString=paymentService.requestApi_POST(new URL(rawUrl),mapper.writeValueAsString(request),tokenString);
        UABCheckPhResponse result= mapper.readValue(resultString,UABCheckPhResponse.class);
        if(!result.getRespCode().equals(UABProperties.APIResponseStatus.SUCCESS.getValue())){
            log.error("INVALID_UAB_RESPONSE >>> "+resultString);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, result.getRespDescription());
        }

        //TODO: check hash value response
        String resultHash=securityManager.generateUABHashHmac(result.getSignatureString());
        if(!resultHash.equals(result.getHashValue())){
            log.error("INVALID_UAB_RESPONSE >>>"+resultString);
            throw new GeneralException(HttpStatus.NON_AUTHORITATIVE_INFORMATION, "Invalid Server Response!");
        }

        return result;
    }

    public UABEnquiryPaymentResponse enquiryPayment(String invoiceNo,String sequenceNo,String amount,String remark,String walletUserId, String tokenString) throws IOException{
        ObjectMapper mapper=new ObjectMapper();
        UABEnquiryPaymentRequest request=new UABEnquiryPaymentRequest(uabProperties.getChannel(),uabProperties.getAppName(),uabProperties.getUser(),
                invoiceNo,sequenceNo,amount,remark,walletUserId, uabProperties.getEnquiryCallbackUrl(), paymentProperties.getExpiredSeconds(),"");
        request.setHashValue(securityManager.generateUABHashHmac(request.getSignatureString()));
        String rawUrl=uabProperties.getEnquiryPaymentUrl();

        String resultString=paymentService.requestApi_POST(new URL(rawUrl),mapper.writeValueAsString(request),tokenString);
        UABEnquiryPaymentResponse result= mapper.readValue(resultString,UABEnquiryPaymentResponse.class);
        if(!result.getRespCode().equals(UABProperties.APIResponseStatus.SUCCESS.getValue())){
            log.error("INVALID_UAB_RESPONSE >>> "+resultString);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, result.getRespDescription());
        }

        //TODO: check hash value response
        String resultHash=securityManager.generateUABHashHmac(result.getSignatureString());
        if(!resultHash.equals(result.getHashValue())){
            log.error("INVALID_UAB_RESPONSE >>>"+resultString);
            throw new GeneralException(HttpStatus.NON_AUTHORITATIVE_INFORMATION, "Invalid Server Response!");
        }

        return result;
    }

    public UABResponsePaymentResponse enquiryPaymentCallback(UABResponsePaymentRequest request)throws IOException{
        ObjectMapper mapper=new ObjectMapper();
        String requestHash=securityManager.generateUABHashHmac(request.getSignatureString());
        if(!requestHash.equals(request.getHashValue())){
            log.error("INVALID_UAB_RESPONSE >>>"+mapper.writeValueAsString(request));
            throw new GeneralException(HttpStatus.NON_AUTHORITATIVE_INFORMATION, "Invalid Server Response!");
        }

        String invoiceNo=request.getInvoiceNo();
        //TODO: call repository with invoice no and update status
        //TODO: check response transaction status code "000" success
        List<Map<String,String>> itemList=new ArrayList<>();
        itemList.add(this.getResponseItemData("1","1","1"));
        itemList.add(this.getResponseItemData("2","1","1"));
        String itemListStr=mapper.writeValueAsString(itemList);
        UABResponsePaymentResponse response=new UABResponsePaymentResponse(request.getReferIntegrationId(), UABResponsePaymentResponse.DataType.Data,
                "",itemListStr,"Success",UABProperties.TransactionStatus.SUCCESS.getValue(),"");

        response.setHashValue(securityManager.generateUABHashHmac(response.getSignatureString()));

        return response;
    }

    private Map<String,String> getResponseItemData(String id,String qty,String price){
        Map<String,String> item=new HashMap<>();
        item.put("ItemId",id);
        item.put("Quantity",qty);
        item.put("EachPrice",price);
        return item;
    }

    public UABCheckTransactionResponse checkTransactionStatus(String ReferIntegrationID,String tokenString)throws IOException{
        ObjectMapper mapper=new ObjectMapper();
        UABCheckTransactionRequest request=new UABCheckTransactionRequest(uabProperties.getUser(),uabProperties.getChannel(),uabProperties.getAppName(),ReferIntegrationID,"");
        request.setHashValue(securityManager.generateUABHashHmac(request.getSignatureString()));
        String rawUrl=uabProperties.getCheckTransactionStatus();
        String resultString=paymentService.requestApi_POST(new URL(rawUrl),mapper.writeValueAsString(request),tokenString);
        UABCheckTransactionResponse result= mapper.readValue(resultString,UABCheckTransactionResponse.class);
        if(!result.getRespCode().equals(UABProperties.APIResponseStatus.SUCCESS.getValue())){
            log.error("INVALID_UAB_RESPONSE >>> "+resultString);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, result.getRespDescription());
        }

        //TODO: check hash value response
        String resultHash=securityManager.generateUABHashHmac(result.getSignatureString());
        if(!resultHash.equals(result.getHashValue())){
            log.error("INVALID_UAB_RESPONSE >>>"+resultString);
            throw new GeneralException(HttpStatus.NON_AUTHORITATIVE_INFORMATION, "Invalid Server Response!");
        }

        return result;
    }


}
