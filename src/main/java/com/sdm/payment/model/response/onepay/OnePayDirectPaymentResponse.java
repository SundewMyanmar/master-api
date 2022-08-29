package com.sdm.payment.model.response.onepay;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnePayDirectPaymentResponse implements Serializable {
    private static final long serialVersionUID = -4669395741810081905L;

    @JsonProperty("Channel")
    private String Channel;

    @JsonProperty("ReferIntegrationId")
    private String ReferIntegrationId;

    @JsonProperty("MerchantUserId")
    private String MerchantUserId;

    @JsonProperty("Amount")
    private String Amount;

    @JsonProperty("Remark")
    private String Remark;

    @JsonProperty("RespDescription")
    private String RespDescription;

    @JsonProperty("RespCode")
    private String RespCode;

    @JsonProperty("HashValue")
    private String HashValue;

    @JsonIgnore
    public String getSignatureString() {
        return Channel + ReferIntegrationId + MerchantUserId + Amount + Remark + RespDescription + RespCode;
    }
}
