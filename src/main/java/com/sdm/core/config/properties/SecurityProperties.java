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

    @DurationUnit(ChronoUnit.MINUTES)
    private Duration otpLife = Duration.ofMinutes(10);

    @DurationUnit(ChronoUnit.DAYS)
    private Duration authTokenLife = Duration.ofDays(30);

    private String jwtKey = "";

    private boolean requireConfirm = false;

    private int authFailedCount = 3;

    @DurationUnit(ChronoUnit.MINUTES)
    private Duration blockedTime = Duration.ofMinutes(30);

    private String cookieDomain = "";

    private String cookieDomainPattern = "";

    private String cookiePath = "";

    private Integer clientRole = 3;

    private Integer adminRole = 2;

    private boolean csrfEnable = true;
}