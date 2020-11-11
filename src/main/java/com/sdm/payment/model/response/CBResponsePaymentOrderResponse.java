package com.sdm.payment.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
