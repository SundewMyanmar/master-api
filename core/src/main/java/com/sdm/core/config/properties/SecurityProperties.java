package com.sdm.core.config.properties;

import com.sdm.core.util.annotation.SettingFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SettingFile("security-config.json")
public class SecurityProperties {
    private Set<Integer> ownerIds = Set.of(1);

    private int authTokenDayOfLife = 30;

    private boolean requireConfirm = false;

    private int authFailedCount = 3;

    private int authFailedMinuteOfBlock = 30;

    private int clientRole = 2;

    private int adminRole = 1;

    private String cookieDomain = "";

    private String cookieDomainPattern = "";

    private String cookiePath = "";

    private boolean csrfEnable = true;
}