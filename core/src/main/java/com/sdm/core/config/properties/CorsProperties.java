package com.sdm.core.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
@ConfigurationProperties(prefix = "com.sdm.cors")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CorsProperties {
    private String[] allowedOriginPatterns;

    private String[] allowedOrigins = {"*"};

    private String[] allowedMethods = {"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"};

    private String[] allowedHeaders = {"authorization", "content-type", "x-requested-with", "x-xsrf-token", "x-forwarded-for", "accept"};

    private String[] exposedHeaders = {"xsrf-token"};

    private Boolean allowedCredential = true;

    @DurationUnit(ChronoUnit.MINUTES)
    private Duration maxAge = Duration.ofHours(1);
}