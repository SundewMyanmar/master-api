package com.sdm.core.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Id;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

public class AuthInfo implements UserDetails {

    @Id
    private String token;
    private long userId;
    private String deviceId;
    private String deviceOs;
    private Date expired;
    Collection<GrantedAuthority> authorities;

    public AuthInfo() {
    }

    public AuthInfo(long userId, String token, String deviceId, Date expired, Collection<GrantedAuthority> authorities) {
        this.userId = userId;
        this.token = token;
        this.deviceId = deviceId;
        this.expired = expired;
        this.authorities = authorities;
    }

    public long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceOs() {
        return deviceOs;
    }

    public void setDeviceOs(String deviceOs) {
        this.deviceOs = deviceOs;
    }

    public Date getExpired() {
        return expired;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setExpired(Date expired) {
        this.expired = expired;
    }

    public void setAuthorities(Collection<GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public void addAuthority(String authority) {
        if (authorities == null) {
            this.authorities = new HashSet<>();
        }
        this.authorities.add(new SimpleGrantedAuthority(authority));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.token;
    }

    @Override
    public String getUsername() {
        return this.deviceId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return expired.after(new Date());
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
