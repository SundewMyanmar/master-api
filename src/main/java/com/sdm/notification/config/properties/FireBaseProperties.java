package com.sdm.notification.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "com.sdm.firebase")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FireBaseProperties {
    private String serviceJson = "";
    private String projectUrl = "";
}
