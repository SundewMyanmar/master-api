package com.sdm.auth.config.properties;


import com.sdm.core.util.annotation.SettingFile;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SettingFile(value = "facebook-config.json", icon = "facebook")
public class FacebookProperties implements Serializable {

    private String appId;

    private String appSecret;

    private String accessToken;

    private String authFields = "id,name,email,picture.width(512),gender";

    private String graphUrl = "https://graph.facebook.com/v14.0/";
}
