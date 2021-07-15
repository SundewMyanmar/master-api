package com.sdm.sms.config.properties;


import com.sdm.core.util.Globalizer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "com.sdm.telenor")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TelenorSmsProperties {
    private String apiUrl = "";
    private String clientId = "";
    private String clientSecret = "";
    private String userName = "";
    private String password = "";
    private String senderId = "";
    private Integer expiredIn = 86400;

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
