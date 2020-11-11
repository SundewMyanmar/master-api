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
    private String appSecret;

    private String tokenServerUrl="https://oauth2.googleapis.com/token";

    private String redirectUrl="http://localhost";

    @Autowired
    private PathProperties pathProperties;
}
