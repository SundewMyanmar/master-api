package com.sdm.payment.model.response.kbzpay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KBZPayPaymentResponse implements Serializable {
    private static final long serialVersionUID = 6024000552226634226L;

    @JsonProperty("Response")
    private KBZPayResponse response;
}
