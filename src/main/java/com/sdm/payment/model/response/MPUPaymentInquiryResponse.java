package com.sdm.payment.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MPUPaymentInquiryResponse implements Serializable {
    private static final long serialVersionUID = -7978419822483526035L;

    @JsonProperty("merchantID")
    private String merchantID;

    @JsonProperty("respCode")
    private String respCode;

    @JsonProperty("pan")
    private String pan;

    @JsonProperty("amount")
    private Long amount;

    @JsonProperty("invoiceNo")
    private String invoiceNo;

    @JsonProperty("tranRef")
    private String tranRef;

    @JsonProperty("approvalCode")
    private String approvalCode;

    @JsonProperty("dateTime")
    private Long dateTime;

    @JsonProperty("status")
    private String status;

    @JsonProperty("failReason")
    private String failReason;

    @JsonProperty("categoryCode")
    private String categoryCode;

    @JsonProperty("userDefined1")
    private String userDefined1;

    @JsonProperty("userDefined2")
    private String userDefined2;

    @JsonProperty("userDefined3")
    private String userDefined3;

    @JsonProperty("hashValue")
    private String hashValue;

    @JsonIgnore
    public String getSignatureString(){
        return merchantID+respCode+pan+amount+invoiceNo+tranRef+approvalCode+dateTime+status+failReason+categoryCode+userDefined1+userDefined2+userDefined3;
    }
}
