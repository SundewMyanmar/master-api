package com.sdm.core.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecurityProperties {
    private Set<Integer> ownerIds = Set.of(1);

    @DurationUnit(ChronoUnit.DAYS)
    private Duration authTokenLife = Duration.ofDays(30);

    private boolean requireConfirm = false;

    private int authFailedCount = 3;

    @DurationUnit(ChronoUnit.MINUTES)
    private Duration blockedTime = Duration.ofMinutes(30);

    private Integer clientRole = 2;

    private Integer adminRole = 1;

    private String cookieDomain = "";

    private String cookieDomainPattern = "";

    private String cookiePath = "";

    private boolean csrfEnable = true;
}