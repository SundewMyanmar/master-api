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
public class OnePayVerifyPhRequest implements Serializable {
    private static final long serialVersionUID = -8148157340362888754L;

    @NotBlank
    @Size(min = 1, max = 25)
    @JsonProperty("Channel")
    private String Channel;

    @NotBlank
    @Size(min = 1, max = 25)
    @JsonProperty("MerchantUserId")
    private String MerchantUserId;

    @NotBlank
    @Size(min = 1, max = 50)
    @JsonProperty("OnepayPhoneNo")
    private String OnepayPhoneNo;

    @NotBlank
    @Size(min = 1, max = 40)
    @JsonProperty("HashValue")
    private String HashValue;

    @JsonIgnore
    public String getSignatureString() {
        return Channel + MerchantUserId + OnepayPhoneNo;
    }
}
