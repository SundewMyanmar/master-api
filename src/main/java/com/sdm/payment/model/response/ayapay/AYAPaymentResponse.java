package com.sdm.payment.model.response.ayapay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AYAPaymentResponse implements Serializable {
    private static final long serialVersionUID = -3656186263639890175L;

    @JsonProperty("err")
    private String err;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private Map<String, Object> data;
}
