package com.sdm.payment.model.request.kbzpay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KBZPaymentResponseRequest implements Serializable {
    private static final long serialVersionUID = 7575706660951035831L;

    @JsonProperty("appid")
    private String appId;

    @JsonProperty("notify_time")
    private String notifyTime;

    @JsonProperty("merch_code")
    private String merchCode;

    @JsonProperty("merch_order_id")
    private String merchOrderId;

    @JsonProperty("mm_order_id")
    private String mmOrderId;

    @JsonProperty("total_amount")
    private String totalAmount;

    @JsonProperty("trans_currency")
    private String transCurrency;

    @JsonProperty("trade_status")
    private String tradeStatus;

    @JsonProperty("trans_end_time")
    private String transEndTime;

    @JsonProperty("callback_info")
    private String callbackInfo;

    @JsonProperty("nonce_str")
    private String nonceStr;

    @JsonProperty("sign")
    private String sign;

    @JsonProperty("sign_type")
    private String signType;
}
