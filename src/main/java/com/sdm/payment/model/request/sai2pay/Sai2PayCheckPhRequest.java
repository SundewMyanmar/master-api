package com.sdm.payment.model.request.sai2pay;

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
public class Sai2PayCheckPhRequest implements Serializable {
    private static final long serialVersionUID = 2479066838685539632L;
    /*
        Channel From Uabpay system
         */
    @NotBlank
    @Size(min = 1, max = 25)
    @JsonProperty("Channel")
    private String Channel;

    /*
    Merchant User ID from Uabpay system
     */
    @NotBlank
    @Size(min = 1, max = 25)
    @JsonProperty("MerchantUserId")
    private String MerchantUserId;

    /*
    Indirect Approach (Website):
    Pass phone-number entered by user in website.
    Direct Approach (PWA within Uabpay):
    Use value of UserID available in following link.
    http://{merchantname}.com/Index?UserID=0979963
    3264;
    E.g. 09799633264 is UabpayPhoneNo.
     */
    @NotBlank
    @Size(min = 1, max = 50)
    @JsonProperty("UabpayPhoneNo")
    private String UabpayPhoneNo;

    /*
    Application Name that merchant want to integrate.
    Eg. saisaipay, uabpay, uabpayplus and else
     */
    @NotBlank
    @Size(min = 1, max = 50)
    @JsonProperty("AppName")
    private String AppName;

    /*
    Value must be Upper Case. For 1.0,
    HMACSHA1 cryptographic hash value of:
    Channel + MerchantUserId + UabpayPhoneNo+
    AppName
     */
    @NotBlank
    @Size(min = 1, max = 40)
    @JsonProperty("HashValue")
    private String HashValue;

    @JsonIgnore
    public String getSignatureString() {
        return Channel + MerchantUserId + AppName + UabpayPhoneNo;
    }
}
