package com.sdm.auth.config.properties;

import com.sdm.core.util.annotation.SettingFile;
import com.sdm.core.util.annotation.Encrypt;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SettingFile(value="apple-auth-config.json",icon="apple")
public class AppleProperties {
    @Encrypt
    private String teamId;

    private String appId;

    @Encrypt
    private String privateKey;
}
