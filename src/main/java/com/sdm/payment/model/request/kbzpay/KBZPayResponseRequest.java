package com.sdm.payment.model.request.kbzpay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KBZPayResponseRequest implements Serializable {
    private static final long serialVersionUID = 7297024891084454889L;
    @JsonProperty("Request")
    private KBZPaymentResponseRequest request;
}
