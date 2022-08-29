package com.sdm.payment.model.response.ayapay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AYAPayCallbackData implements Serializable {
    private static final long serialVersionUID = -1078881390666406698L;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("externalAdditionalData")
    private String externalAdditionalData;

    @JsonProperty("fees")
    private Map<String, Integer> fees;

    @JsonProperty("name")
    private String name;

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("totalAmount")
    private String totalAmount;

    @JsonProperty("merchant")
    private Map<String, Object> merchant;

    @JsonProperty("referenceNumber")
    private String referenceNumber;

    @JsonProperty("customer")
    private Map<String, Object> customer;

    //done:paid, failed:failed
    @JsonProperty("status")
    private String status;

    @JsonProperty("externalTransactionId")
    private String externalTransactionId;

    @JsonProperty("transRefId")
    private String transRefId;
}
