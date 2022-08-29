package com.sdm.payment.model.request.ayapay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AYAPaymentRequest implements Serializable {
    private static final long serialVersionUID = 8526335007367088338L;

    @JsonProperty("customerPhone")
    private String customerPhone;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("externalTransactionId")
    private String externalTransactionId;

    @JsonProperty("externalAdditionalData")
    private String externalAdditionalData;
}
