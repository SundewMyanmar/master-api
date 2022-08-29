package com.sdm.payment.model.response.cbpay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CBResponsePaymentOrderResponse implements Serializable {
    private static final long serialVersionUID = -3413213863148158259L;

    @JsonProperty("responseCode")
    private String responseCode;

    @JsonProperty("responseMsg")
    private String responseMsg;
}
