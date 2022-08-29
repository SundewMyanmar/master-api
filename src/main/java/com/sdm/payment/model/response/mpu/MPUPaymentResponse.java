package com.sdm.payment.model.response.mpu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MPUPaymentResponse implements Serializable {
    private static final long serialVersionUID = 5965445499536336068L;

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

    @JsonProperty("userDefined1")
    private String userDefined1;

    @JsonProperty("userDefined2")
    private String userDefined2;

    @JsonProperty("userDefined3")
    private String userDefined3;

    @JsonProperty("hashValue")
    private String hashValue;

    @JsonIgnore
    public String getSignatureString() {
        return merchantID + respCode + pan + amount + invoiceNo + tranRef + approvalCode + dateTime + status + failReason + userDefined1 + userDefined2 + userDefined3;
    }
}
