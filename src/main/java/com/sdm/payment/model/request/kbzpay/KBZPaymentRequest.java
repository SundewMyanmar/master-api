package com.sdm.payment.model.request.kbzpay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KBZPaymentRequest implements Serializable {
    private static final long serialVersionUID = -6012638647341575762L;
    @JsonProperty("Request")
    private KBZPayRequest request;
}
