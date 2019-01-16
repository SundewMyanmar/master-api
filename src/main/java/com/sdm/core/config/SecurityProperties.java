package com.sdm.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "com.sdm.security")
public class SecurityProperties {

    private Set<Long> ownerIds = new HashSet<>();

    private String encryptSalt = "";

    private String tokenChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private int otpLife = 10;

    private int authTokenLife = 30;

    private String jwtKey = "";

    private boolean requireConfirm = false;

    private String[] publicUrls;

    public Set<Long> getOwnerIds() {
        return ownerIds;
    }

    public void setOwnerIds(Set<Long> ownerIds) {
        this.ownerIds = ownerIds;
    }

    public String getEncryptSalt() {
        return encryptSalt;
    }

    public void setEncryptSalt(String encryptSalt) {
        this.encryptSalt = encryptSalt;
    }

    public String getTokenChars() {
        return tokenChars;
    }

    public void setTokenChars(String tokenChars) {
        this.tokenChars = tokenChars;
    }

    public int getOtpLife() {
        return otpLife;
    }

    public void setOtpLife(int otpLife) {
        this.otpLife = otpLife;
    }

    public int getAuthTokenLife() {
        return authTokenLife;
    }

    public void setAuthTokenLife(int authTokenLife) {
        this.authTokenLife = authTokenLife;
    }

    public String getJwtKey() {
        return jwtKey;
    }

    public void setJwtKey(String jwtKey) {
        this.jwtKey = jwtKey;
    }

    public boolean isRequireConfirm() {
        return requireConfirm;
    }

    public void setRequireConfirm(boolean requireConfirm) {
        this.requireConfirm = requireConfirm;
    }

    public String[] getPublicUrls() {
        return publicUrls;
    }

    public void setPublicUrls(String[] publicUrls) {
        this.publicUrls = publicUrls;
    }
}
