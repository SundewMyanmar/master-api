package com.sdm.payment.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CBResponsePaymentOrderRequest implements Serializable {
    private static final long serialVersionUID = -6706523973003124826L;

    @JsonProperty("generateRefOrder")
    private String generateRefOrder;

    @JsonProperty("transactionid")
    private String transactionid;

    @JsonProperty("transacationDateTime")
    private String transacationDateTime;

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("transactionDateTime")
    private String transactionDateTime;

    @JsonProperty("ecommerceId")
    private String ecommerceId;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("feeAmount")
    private String feeAmount;

    @JsonProperty("discount")
    private String discount;

    @JsonProperty("totalAmount")
    private String totalAmount;

    @JsonProperty("signature")
    private String signature;

    @JsonProperty("transactionStatus")
    private String transactionStatus;

//    @JsonIgnore
//    public String getSignatureString(){
//        return authenToken+ecommerceId+subMerId+orderId+amount+currency;
//    }

    public String getTransactionStatusString(){
        switch(transactionStatus){
            case "P":
                return "PENDING";
            case "S":
                return "SUCCESS";
            case "F":
                return "FAIL";
            case "E":
                return "EXPIRED";
            default:
                return "INVALID";
        }
    }
}
