package com.sdm.payment.model.request.kbzpay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KBZPayResponseRequest implements Serializable {
    private static final long serialVersionUID = 7297024891084454889L;
    @JsonProperty("Request")
    private KBZPaymentResponseRequest request;
}
