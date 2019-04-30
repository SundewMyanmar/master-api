package com.sdm.core.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "com.sdm.firebase")
public class FireBaseProperties {

    private String serviceJson = "";

    private String projectUrl = "";

    public String getServiceJson() {
        return serviceJson;
    }

    public void setServiceJson(String serviceJson) {
        this.serviceJson = serviceJson;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }
}
