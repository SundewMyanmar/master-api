package com.sdm.payment.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UABEnquiryPaymentResponse implements Serializable {
    private static final long serialVersionUID = -1707678999900371273L;
    /*
        Channel from Uabpay System
         */
    @JsonProperty("Channel")
    private String Channel;
    /*
    Sequence No From Request
     */
    @JsonProperty("ReferIntegrationId")
    private String ReferIntegrationId;
    /*
    Merchant User ID from Uabpay System
     */
    @JsonProperty("MerchantUserId")
    private String MerchantUserId;
    /*
    Must be Number eg./(5000)
     */
    @JsonProperty("Amount")
    private String Amount;
    /*
    Remark from Merchant
     */
    @JsonProperty("Remark")
    private String Remark;
    /*
    Response Description eg. Success , Fail
     */
    @JsonProperty("RespDescription")
    private String RespDescription;
    /*
    Response Code
    eg. 000, 001,002,012,909
     */
    @JsonProperty("RespCode")
    private String RespCode;
    /*
    Value must be Upper Case. For 1.0,
    HMACSHA1 cryptographic hash value
    of:
    Channel + ReferIntegrationId +
    MerchantUserId + Amount + Remark +
    RespDescription + RespCode.
     */
    @JsonProperty("HashValue")
    private String HashValue;
    //TODO: Check back hash Value

    @JsonIgnore
    public String getSignatureString() {
        return Channel + ReferIntegrationId + MerchantUserId + Amount + Remark + RespDescription + RespCode;
    }
}
