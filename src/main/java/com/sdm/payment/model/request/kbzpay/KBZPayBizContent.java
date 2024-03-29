package com.sdm.payment.model.request.kbzpay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KBZPayBizContent implements Serializable {
    private static final long serialVersionUID = 327947301257493104L;
    /*
    Specifies an Official Account Code assigned by KBZPay
     */
    @JsonProperty("merch_code")
    private String merchCode;

    /*
    The order number generated by the merchant side. It must be in the form of letters, numbers, and underscores. Other special characters are not allowed.
     */
    @JsonProperty("merch_order_id")
    private String merchOrderId;

    @JsonProperty("appid")
    private String appId;

    /*
    APP Trade type. The option are APP
     */
    @JsonProperty("trade_type")
    private String tradeType;

    /*
    Offering name.
     */
    @JsonProperty("title")
    private String title;

    @JsonProperty("total_amount")
    private String totalAmount;

    /*
    Three-letter code complying with international standards, for example, MMK (Kyat).
     */
    @JsonProperty("trans_currency")
    private String transCurrency;

    /*
    After the payment is successful, KBZPay will notify the merchant server of the callback request to return this field as it is, and the
    merchant can pass the business parameters according to its own needs. Must be in URL encoding format.
     */
    @JsonProperty("callback_info")
    private String callbackInfo;
}
