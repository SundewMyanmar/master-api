package com.sdm.core.config.properties;

import com.sdm.core.util.annotation.SettingFile;

import java.io.Serializable;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SettingFile(value = "security-config.json", icon = "security")
public class SecurityProperties implements Serializable {
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