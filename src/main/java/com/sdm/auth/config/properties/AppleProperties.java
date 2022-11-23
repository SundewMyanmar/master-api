package com.sdm.auth.config.properties;

import com.sdm.core.util.annotation.Encrypt;
import com.sdm.core.util.annotation.SettingFile;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SettingFile(value = "apple-auth-config.json", icon = "apple")
public class AppleProperties implements Serializable {
    @Encrypt
    private String teamId;

    private String appId;

    @Encrypt
    private String privateKey;
}
