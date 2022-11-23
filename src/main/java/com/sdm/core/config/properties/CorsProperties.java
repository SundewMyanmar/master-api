package com.sdm.core.config.properties;

import com.sdm.core.util.annotation.SettingFile;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SettingFile(value = "cors-config.json", icon = "settings_input_composite")
public class CorsProperties implements Serializable {
    private String[] allowedOriginPatterns;

    private String[] allowedOrigins = {"*"};

    private String[] allowedMethods = {"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"};

    private String[] allowedHeaders = {"authorization", "content-type", "x-requested-with", "x-xsrf-token", "x-forwarded-for", "accept"};

    private String[] exposedHeaders = {"xsrf-token"};

    private Boolean allowedCredential = true;

    private Long maxAge = 1L;
}