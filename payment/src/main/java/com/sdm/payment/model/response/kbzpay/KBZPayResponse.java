package com.sdm.payment.model.response.kbzpay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KBZPayResponse implements Serializable {
    private static final long serialVersionUID = -2879690486784382874L;

    @JsonProperty("result")
    private String result;

    @JsonProperty("code")
    private String code;

    @JsonProperty("msg")
    private String msg;

    @JsonProperty("nonce_str")
    private String nonceStr;

    @JsonProperty("sign")
    private String sign;

    @JsonProperty("sign_type")
    private String signType;

    @JsonProperty("prepay_id")
    private String prepayId;

    @JsonProperty("merch_order_id")
    private String merchOrderId;
}
