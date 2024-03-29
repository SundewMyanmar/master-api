package com.sdm.payment.model.request.mpu;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MPUPaymentVoidRequest implements Serializable {
    private static final long serialVersionUID = 8130837311022632249L;

    @JsonProperty("merchantID")
    private String merchantID;

    @JsonProperty("invoiceNo")
    private String invoiceNo;

    @JsonProperty("actionType")
    private String actionType;

    @JsonProperty("amount")
    private Long amount;

    @JsonProperty("currencyCode")
    private Long currencyCode;

    @JsonProperty("hashValue")
    private String hashValue;

    public String getSignatureString() {
        return actionType + amount + currencyCode + invoiceNo + merchantID;
    }
}
