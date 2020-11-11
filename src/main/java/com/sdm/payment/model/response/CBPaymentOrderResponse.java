package com.sdm.payment.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
