package com.sdm.payment.model.request.sai2pay;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sai2PayResponsePaymentRequest implements Serializable {
    private static final long serialVersionUID = -7767167953041515121L;
    /*
        Merchant User ID From Uabpay System
         */
    @Size(min = 1, max = 25)
    @JsonProperty("MerchantUserId")
    private String MerchantUserId;
    /*
    Channel from Uabpay System
     */
    @Size(min = 1, max = 25)
    @JsonProperty("Channel")
    private String Channel;
    /*
    Application Name that merchant want to
    integrate.
    Eg. saisaipay, uabpay, uabpayplus and else
     */
    @Size(min = 1, max = 20)
    @JsonProperty("AppName")
    private String AppName;
    /*
    Sequence Number For Transaction
     */
    @Size(min = 1, max = 50)
    @JsonProperty("ReferIntegrationId")
    private String ReferIntegrationId;
    /*
    Invoice Number
     */
    @Size(min = 1, max = 30)
    @JsonProperty("InvoiceNo")
    private String InvoiceNo;
    /*
    Must be Number ex
    (5000)
     */
    @Size(min = 1, max = 12)
    @JsonProperty("Amount")
    private String Amount;
    /*
    TransactionStatus from Merchant
    eg. 000, 014,012
     */
    @Size(min = 1, max = 5)
    @JsonProperty("TransactionStatus")
    private String TransactionStatus;
    /*
    ENO No from Uabpay System
     */
    @Size(min = 1, max = 20)
    @JsonProperty("TransactionID")
    private String TransactionID;
    /*
    Value must be Upper Case. For 1.0,
    HMACSHA1 cryptographic hash value of:
    MerchantUserId+Channel + AppName
    +ReferIntegrationID +
    InvoiceNo + Amount + TransactionStatus +
    TransactionID
     */
    @Size(min = 1, max = 40)
    @JsonProperty("HashValue")
    private String HashValue;

    @JsonIgnore
    public String getSignatureString() {
        return MerchantUserId + Channel + AppName + Amount + ReferIntegrationId + InvoiceNo + TransactionStatus + TransactionID;
//        return MerchantUserId + Channel + AppName + ReferIntegrationId + InvoiceNo + Amount + TransactionStatus + TransactionID;
    }
}
