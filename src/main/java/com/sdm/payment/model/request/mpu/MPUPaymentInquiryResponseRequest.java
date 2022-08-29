package com.sdm.payment.model.request.mpu;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MPUPaymentInquiryResponseRequest implements Serializable {
    private static final long serialVersionUID = -3175101209649132393L;

    @JsonProperty("invoiceNo")
    private String invoiceNo;

    @JsonProperty("merchantID")
    private String merchantID;

    @JsonProperty("respCode")
    private String respCode;

    @JsonProperty("pan")
    private String pan;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("tranRef")
    private String tranRef;

    @JsonProperty("approvalCode")
    private String approvalCode;

    @JsonProperty("dateTime")
    private String dateTime;

    @JsonProperty("status")
    private String status;

    @JsonProperty("failReason")
    private String failReason;

    @JsonProperty("userDefined1")
    private String userDefined1;

    @JsonProperty("userDefined2")
    private String userDefined2;

    @JsonProperty("userDefined3")
    private String userDefined3;

    @JsonProperty("categoryCode")
    private String categoryCode;

    @JsonProperty("hashValue")
    private String hashValue;
}
