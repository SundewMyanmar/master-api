package com.sdm.payment.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UABEnquiryPaymentRequest implements Serializable {
    private static final long serialVersionUID = 6119082211469968046L;
    /*
        Channel From Uabpay System
         */
    @NotBlank
    @Size(min = 1, max = 25)
    @JsonProperty("Channel")
    private String Channel;

    /*
    Application Name that merchant want to integrate. Eg. saisaipay, uabpay, uabpayplus and else
     */
    @NotBlank
    @Size(min = 1, max = 20)
    @JsonProperty("AppName")
    private String AppName;

    /*
    Merchant User ID from Uabpay System
     */
    @NotBlank
    @Size(min = 1, max = 25)
    @JsonProperty("MerchantUserId")
    private String MerchantUserId;

    /*
    Invoice Number
     */
    @NotBlank
    @Size(min = 1, max = 30)
    @JsonProperty("InvoiceNo")
    private String InvoiceNo;

    /*
    Unique Sequence Number (Length must be between 30 and 50).
    eg.
    DEF11DB87433EB9F33394AE98E3E9 or
    200000000000000000000123
     */
    @NotBlank
    @Size(min = 1, max = 30)
    @JsonProperty("SequenceNo")
    private String SequenceNo;

    /*
    Must be Number ex (5000)
     */
    @NotBlank
    @Size(min = 1, max = 12)
    @JsonProperty("Amount")
    private String Amount;
    /*
    Remark from Merchant
     */
    @Size(min = 0, max = 255)
    @JsonProperty("Remark")
    private String Remark;

    /*
    Indirect Approach (Website):
    Pass phone-number entered by user in website.
    Direct Approach (PWA within Uabpay):
    Use value of UserID available in following link.
    http://{merchantname}.com/Home?UserID=09799633264;
    E.g. 09799633264 is WalletUserID
     */
    @Size(min = 0, max = 36)
    @JsonProperty("WalletUserID")
    private String WalletUserID;

    /*
    To invoke url after transaction process.
    eg. {merchantname}.com/Reponsetransaction
     */
    @Size(min = 1, max = 255)
    @JsonProperty("CallBackUrl")
    private String CallBackUrl;
    /*
    To limit transaction expired seconds.
    ExpiredSeconds must be greater than 60.
    eg. 180 seconds, 300 seconds
     */
    @JsonProperty("ExpiredSeconds")
    private Integer ExpiredSeconds;
    /*
    Value must be Upper Case. For 1.0, HMACSHA1
    cryptographic hash value of:
    Channel +AppName+ MerchantUserId +
    WalletUserID + Amount + Remark + InvoiceNo + SequenceNo
    + CallBackUrl + ExpiredSeconds.
     */
    @Size(min = 1, max = 40)
    @JsonProperty("HashValue")
    private String HashValue;

    @JsonIgnore
    public String getSignatureString() {
        return Channel+AppName+MerchantUserId+WalletUserID+Amount+Remark+InvoiceNo+SequenceNo+CallBackUrl+ExpiredSeconds;
    }
}
