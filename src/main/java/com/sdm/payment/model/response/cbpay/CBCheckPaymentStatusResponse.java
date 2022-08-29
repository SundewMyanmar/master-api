package com.sdm.payment.model.response.cbpay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CBCheckPaymentStatusResponse implements Serializable {
    private static final long serialVersionUID = 2941232906395050653L;

    /**
     * Transaction Status
     * P = pending
     * S = Success
     * F = Fail
     * E = Expired
     */

    @JsonProperty("generateRefOrder")
    private String generateRefOrder;

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("transactionStatus")
    private String transactionStatus;

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("transactionDate")
    private String transactionDate;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("feeAmount")
    private String feeAmount;

    @JsonProperty("discount")
    private String discount;

    @JsonProperty("totalAmount")
    private String totalAmount;

    @JsonProperty("responseCode")
    private String responseCode;

    @JsonProperty("responseMsg")
    private String responseMsg;

    @JsonProperty("code")
    private String code;

    @JsonProperty("msg")
    private String msg;

    public String getTransactionStatusString() {
        switch (transactionStatus) {
            case "P":
                return "PENDING";
            case "S":
                return "SUCCESS";
            case "F":
                return "FAIL";
            case "E":
                return "EXPIRED";
            default:
                return "INVALID";
        }
    }
}
