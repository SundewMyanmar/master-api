package com.sdm.payment.model.request.wavepay;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WavePayResponsePaymentRequest implements Serializable {
    private static final long serialVersionUID = -80628176746304387L;

    public enum PaymentStatus {
        PAYMENT_CONFIRMED,
        TRANSACTION_TIMED_OUT,
        INSUFFICIENT_BALANCE,
        ACCOUNT_LOCKED,
        BILL_COLLECTION_FAILED,
        PAYMENT_REQUEST_CANCELLED,
        SCHEDULER_TRANSACTION_TIMED_OUT
    }

    @Enumerated(EnumType.STRING)
    @JsonProperty("status")
    private PaymentStatus status;

    @JsonProperty("merchantId")
    private String merchantId;

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("merchantReferenceId")
    private String merchantReferenceId;

    @JsonProperty("frontendResultUrl")
    private String frontendResultUrl;

    @JsonProperty("backendResultUrl")
    private String backendResultUrl;

    @JsonProperty("initiatorMsisdn")
    private String initiatorMsisdn;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("timeToLiveSeconds")
    private String timeToLiveSeconds;

    @JsonProperty("paymentDescription")
    private String paymentDescription;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("hashValue")
    private String hashValue;

    @JsonProperty("additionalField1")
    private String additionalField1;

    @JsonProperty("additionalField2")
    private String additionalField2;

    @JsonProperty("additionalField3")
    private String additionalField3;

    @JsonProperty("additionalField4")
    private String additionalField4;

    @JsonProperty("additionalField5")
    private String additionalField5;

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("paymentRequestId")
    private String paymentRequestId;

    @JsonProperty("requestTime")
    private String requestTime;

    @JsonIgnore
    public String getSignatureString() {
        return status + timeToLiveSeconds + merchantId + orderId + amount + backendResultUrl + merchantReferenceId + initiatorMsisdn + transactionId + paymentRequestId + requestTime;
    }
}
