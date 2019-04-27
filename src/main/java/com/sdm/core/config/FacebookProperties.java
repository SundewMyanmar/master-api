package com.sdm.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "com.sdm.facebook")
public class FacebookProperties {

    private String appId = "";

    private String pageAccessToken = "";

    private String webhookToken = "";

    private String graphURL = "";

    /**
     * @return the appId
     */
    public String getAppId() {
        return appId;
    }

    /**
     * @param appId the appId to set
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * @return the graphURL
     */
    public String getGraphURL() {
        return graphURL;
    }

    /**
     * @param graphURL the graphURL to set
     */
    public void setGraphURL(String graphURL) {
        this.graphURL = graphURL;
    }

    /**
     * @return the pageAccessToken
     */
    public String getPageAccessToken() {
        return pageAccessToken;
    }

    /**
     * @param pageAccessToken the pageAccessToken to set
     */
    public void setPageAccessToken(String pageAccessToken) {
        this.pageAccessToken = pageAccessToken;
    }

    /**
     * @return the webhookToken
     */
    public String getWebhookToken() {
        return webhookToken;
    }

    /**
     * @param webhookToken the webhookToken to set
     */
    public void setWebhookToken(String webhookToken) {
        this.webhookToken = webhookToken;
    }

}
