package com.sdm.auth.config.properties;

import com.sdm.core.util.annotation.SettingFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SettingFile("apple-auth-config.json")
public class AppleProperties {
    private String teamId;

    private String appId;

    private String privateKey;
}
