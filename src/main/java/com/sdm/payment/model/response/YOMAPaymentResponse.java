package com.sdm.payment.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YOMAPaymentResponse implements Serializable {
    private static final long serialVersionUID = 599364946382836790L;

    @JsonProperty("status_code")
    private String statusCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("errors")
    private Map<String,String[]> errors;

    private String authenticatedUrl;
}
