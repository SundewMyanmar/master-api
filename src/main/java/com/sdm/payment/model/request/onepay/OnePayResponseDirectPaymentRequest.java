package com.sdm.payment.model.request.onepay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnePayResponseDirectPaymentRequest implements Serializable {
    private static final long serialVersionUID = -8813818358132516414L;

    @JsonProperty("MerchantUserId")
    private String MerchantUserId;

    /**
     * Sequence Number For Transaction
     */
    @JsonProperty("ReferIntegrationId")
    private String ReferIntegrationId;

    @JsonProperty("InvoiceNo")
    private String InvoiceNo;

    @JsonProperty("Amount")
    private String Amount;

    /**
     * TransactionStatus from Merchant
     * eg. 000, 014,012
     */
    @JsonProperty("TransactionStatus")
    private String TransactionStatus;

    /**
     * ENO No from Onepay System
     */
    @JsonProperty("TransactionID")
    private String TransactionID;

    @JsonProperty("HashValue")
    private String HashValue;

    public String getSignatureString() {
        return MerchantUserId + ReferIntegrationId + InvoiceNo + Amount + TransactionStatus + TransactionID;
    }
}
