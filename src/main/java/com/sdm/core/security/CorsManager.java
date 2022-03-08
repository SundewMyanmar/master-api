package com.sdm.core.security;

import com.sdm.core.config.properties.CorsProperties;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.SettingManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@Component
@Log4j2
public class CorsManager {
    @Autowired
    private SettingManager settingManager;

    public CorsProperties getProperties() {
        CorsProperties properties = new CorsProperties();
        try {
            properties = settingManager.loadSetting(CorsProperties.class);
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
        }
        return properties;
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        if (!Globalizer.isNullOrEmpty(this.getProperties().getAllowedOriginPatterns())) {
            configuration.setAllowedOriginPatterns(Arrays.asList(this.getProperties().getAllowedOriginPatterns()));
        } else if (Arrays.stream(this.getProperties().getAllowedOrigins()).anyMatch((value) -> value.equalsIgnoreCase("*"))) {
            configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        } else {
            configuration.setAllowedOrigins(Arrays.asList(this.getProperties().getAllowedOrigins()));
        }

        configuration.setAllowedMethods(Arrays.asList(this.getProperties().getAllowedMethods()));
        configuration.setAllowedHeaders(Arrays.asList(this.getProperties().getAllowedHeaders()));
        configuration.setExposedHeaders(Arrays.asList(this.getProperties().getExposedHeaders()));
        configuration.setMaxAge(this.getProperties().getMaxAge());
        configuration.setAllowCredentials(this.getProperties().getAllowedCredential());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
