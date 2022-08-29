package com.sdm.payment.model.request.onepay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnePayCheckTransactionRequest implements Serializable {
    private static final long serialVersionUID = -5704461274343620747L;

    @Size(min = 1, max = 25)
    @JsonProperty("MerchantUserId")
    private String MerchantUserId;

    @Size(min = 1, max = 50)
    @JsonProperty("ReferIntegrationID")
    private String ReferIntegrationID;

    @Size(min = 1, max = 40)
    @JsonProperty("HashValue")
    private String HashValue;

    public String getSignatureString() {
        return MerchantUserId + ReferIntegrationID;
    }
}
