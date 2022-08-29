package com.sdm.payment.model.response.mpu;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MPUPaymentVoidResponse implements Serializable {
    private static final long serialVersionUID = 3365498724075057445L;

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

    @JsonProperty("hashValue")
    private String hashValue;

    public String getSignatureString() {
        return merchantID + respCode + pan + amount + invoiceNo + tranRef + approvalCode + dateTime + status;
    }
}
