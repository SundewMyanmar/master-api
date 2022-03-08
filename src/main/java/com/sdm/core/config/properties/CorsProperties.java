package com.sdm.core.config.properties;

import com.sdm.core.util.annotation.SettingFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SettingFile("cors-config.json")
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