package com.sdm.payment.model.response.cbpay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CBPaymentOrderResponse implements Serializable {
    private static final long serialVersionUID = 73618817879890588L;

    @JsonProperty("generateRefOrder")
    private String generateRefOrder;

    @JsonProperty("code")
    private String code;

    @JsonProperty("msg")
    private String msg;

    @JsonProperty("deepLinkUrl")
    private String deepLinkUrl;
}
