package com.sdm.payment.model.response.ayapay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AYAPayCallbackResponse implements Serializable {
    private static final long serialVersionUID = -200403403592788350L;

    @JsonProperty("checksum")
    private String checksum;

    @JsonProperty("paymentResult")
    private String paymentResult;

    @JsonProperty("refundResult")
    private String refundResult;
}
