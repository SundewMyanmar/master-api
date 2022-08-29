package com.sdm.telenor.config.properties;


import com.sdm.core.util.Globalizer;
import com.sdm.core.util.annotation.Encrypt;
import com.sdm.core.util.annotation.SettingFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SettingFile(value = "telenor-sms-config.json", icon = "message_processing")
public class TelenorSmsProperties {
    private String apiUrl = "https://prod-apigw.mytelenor.com.mm/";
    @Encrypt
    private String clientId = "";
    @Encrypt
    private String clientSecret = "";
    @Encrypt
    private String userName = "";
    @Encrypt
    private String password = "";
    @Encrypt
    private String senderId = "";
    private Integer expSeconds = 86400;

    public String getPhoneNo(String ph) {
        if (ph == null) return null;
        return "959" + ph;
    }

    //Authorization code (on time code)
    public String getOAuthURL() {
        if (Globalizer.isNullOrEmpty(this.apiUrl)) {
            throw new NullPointerException();
        }

        return this.apiUrl + "oauth/v1/userAuthorize?scope=READ&response_type=code&client_id="
                + Globalizer.encodeUrl(this.clientId);
    }

    //Generation access token (life time default 1 hr, max 86400 24 hour)
    public String getOAuthTokenURL() {
        if (Globalizer.isNullOrEmpty(this.apiUrl)) {
            throw new NullPointerException();
        }
        return this.apiUrl + "oauth/v1/token";
    }

    //Send message
    public String getCommunicationMessageUrl() {
        if (Globalizer.isNullOrEmpty(this.apiUrl)) {
            throw new NullPointerException();
        }
        return this.apiUrl + "v3/mm/en/communicationMessage/send";
    }

    public String getRedirectUri() {
        //TODO: change back to ssl true for live server
        return Globalizer.getCurrentContextPath("/public/sms/telenor/callback", true);
    }
}
