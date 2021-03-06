package com.sdm.core.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "com.sdm.facebook")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacebookProperties {

    private String appId = "";

    private String appSecret = "";

    private String pageAccessToken = "";

    private String webhookToken = "";

    private String graphURL = "";
}
