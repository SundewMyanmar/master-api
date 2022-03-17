package com.sdm.telenor.model.request.telenor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sdm.core.util.annotation.SettingFile;
import com.sdm.core.util.annotation.Encrypt;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SettingFile(value="telenor-sms-token.json",icon="message_processing")
public class TelenorTokenSetting implements Serializable {
    @JsonProperty("status")
    private String status;

    @Encrypt
    @JsonProperty("accessToken")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("tokenType")
    private String tokenType;

    @JsonProperty("expiresIn")
    private String expiresIn;

    @JsonProperty("expiredDate")
    private Date expiredDate;
}
