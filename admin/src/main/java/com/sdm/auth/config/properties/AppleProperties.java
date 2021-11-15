package com.sdm.auth.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "com.sdm.apple")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppleProperties {
    private String teamId;

    private String appId;

    private String apiKey;
}
