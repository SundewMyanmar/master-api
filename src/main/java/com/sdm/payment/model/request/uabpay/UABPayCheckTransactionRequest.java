package com.sdm.payment.model.request.uabpay;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UABPayCheckTransactionRequest implements Serializable {
    private static final long serialVersionUID = 1459901265349767546L;
    /*
        Merchant User ID From Uabpay System
         */
    @Size(min = 1, max = 25)
    @JsonProperty("MerchantUserId")
    private String MerchantUserId;
    /*
    Channel from Uabpay System
     */
    @Size(min = 1, max = 25)
    @JsonProperty("Channel")
    private String Channel;

    /*
    Application Name that merchant want to
    integrate.
    Eg. saisaipay, uabpay, uabpayplus and else
     */
    @Size(min = 1, max = 20)
    @JsonProperty("AppName")
    private String AppName;

    /*
    Sequence Number For Transaction
     */
    @Size(min = 1, max = 50)
    @JsonProperty("ReferIntegrationID")
    private String ReferIntegrationID;

    /*
    Value must be Upper Case. For 1.0,
    HMACSHA1 cryptographic hash value of:
    MerchantUserId +Channel + AppName+
    ReferIntegrationID
     */
    @Size(min = 1, max = 40)
    @JsonProperty("HashValue")
    private String HashValue;

    @JsonIgnore
    public String getSignatureString() {
        return MerchantUserId + Channel + AppName + ReferIntegrationID;
    }
}
