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
public class AGDDirectPaymentResponse implements Serializable {
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
    public String getSignatureString(){
        return Channel+ReferIntegrationId+MerchantUserId+Amount+Remark+RespDescription+RespCode;
    }
}
