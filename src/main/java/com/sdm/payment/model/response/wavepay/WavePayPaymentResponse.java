package com.sdm.payment.model.response.wavepay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WavePayPaymentResponse implements Serializable {
    private static final long serialVersionUID = 599364946382836790L;

    @JsonProperty("status_code")
    private String statusCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("errors")
    private Map<String, String[]> errors;

    private String authenticatedUrl;
}
