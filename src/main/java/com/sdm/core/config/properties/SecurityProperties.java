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
@ConfigurationProperties(prefix = "com.sdm.security")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecurityProperties {

    private Set<Integer> ownerIds = new HashSet<>();

    private String encryptSalt = "";

    private String tokenChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @DurationUnit(ChronoUnit.MINUTES)
    private Duration otpLife = Duration.ofMinutes(10);

    @DurationUnit(ChronoUnit.DAYS)
    private Duration authTokenLife = Duration.ofDays(30);

    private String jwtKey = "";

    private boolean requireConfirm = false;
}