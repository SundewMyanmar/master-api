package com.sdm.payment.model.response.kbzpay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KBZPayPaymentResponse implements Serializable {
    private static final long serialVersionUID = 6024000552226634226L;

    @JsonProperty("Response")
    private KBZPayResponse response;
}
