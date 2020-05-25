package com.sdm.core.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "com.sdm.path")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PathProperties {
    private String upload = "/var/www/master-api/upload/";
}
