package com.sdm.core.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "com.sdm.google")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleProperties {
    @Autowired
    private PathProperties pathProperties;

    public String getClientSecretFilePath(String clientId){
        return pathProperties.getRoot()+clientId+".json";
    }

    public String getTokenServerUrl(){
        return "https://oauth2.googleapis.com/token";
    }

    public String getRedirectUrl(){
        return "http://localhost";
    }
}
