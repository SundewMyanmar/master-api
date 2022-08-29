package com.sdm.payment.model.request.onepay;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnePayDirectPaymentRequest implements Serializable {
    private static final long serialVersionUID = -6602981525862215909L;

    @NotBlank
    @Size(min = 1, max = 5)
    @JsonProperty("Version")
    private String Version;

    @NotBlank
    @Size(min = 1, max = 25)
    @JsonProperty("Channel")
    private String Channel;

    @NotBlank
    @Size(min = 1, max = 25)
    @JsonProperty("MerchantUserId")
    private String MerchantUserId;

    @NotBlank
    @Size(min = 1, max = 30)
    @JsonProperty("InvoiceNo")
    private String InvoiceNo;

    @NotBlank
    @Size(min = 30, max = 50)
    @JsonProperty("SequenceNo")
    private String SequenceNo;

    @NotBlank
    @Size(min = 1, max = 12)
    @JsonProperty("Amount")
    private String Amount;

    @NotBlank
    @Size(min = 0, max = 255)
    @JsonProperty("Remark")
    private String Remark;

    @NotBlank
    @Size(min = 1, max = 36)
    @JsonProperty("WalletUserID")
    private String WalletUserID;

    @NotBlank
    @Size(min = 1, max = 36)
    @JsonProperty("CallBackUrl")
    private String CallBackUrl;

    @NotBlank
    @JsonProperty("ExpiredSeconds")
    private Integer ExpiredSeconds;

    @NotBlank
    @Size(min = 1, max = 40)
    @JsonProperty("HashValue")
    private String HashValue;

    @JsonIgnore
    public String getSignatureString() {
        return Version + Channel + MerchantUserId + WalletUserID + Amount + Remark + InvoiceNo + SequenceNo + CallBackUrl + ExpiredSeconds;
    }
}
