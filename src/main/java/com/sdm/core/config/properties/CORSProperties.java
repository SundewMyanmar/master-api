package com.sdm.core.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "com.sdm.cors")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CORSProperties {
    private String allowedOrigins = "*";

    private String allowedMethods = "GET, PUT, POST, DELETE, OPTIONS, HEAD";

    private Boolean allowedCredentials = true;

    private String allowedHeaders = "authorization, content-type, xsrf-token, accept";

    private String exposedHeaders = "xsrf-token";

    private long maxAge = 36000;
}