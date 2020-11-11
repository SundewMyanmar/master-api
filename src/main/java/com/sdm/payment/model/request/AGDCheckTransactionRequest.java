package com.sdm.payment.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AGDCheckTransactionRequest implements Serializable {
    private static final long serialVersionUID = -5704461274343620747L;

    @Size(min=1, max = 25)
    @JsonProperty("MerchantUserId")
    private String MerchantUserId;

    @Size(min=1, max=50)
    @JsonProperty("ReferIntegrationID")
    private String ReferIntegrationID;

    @Size(min=1, max=40)
    @JsonProperty("HashValue")
    private String HashValue;

    public String getSignatureString(){return MerchantUserId+ReferIntegrationID;}
}
