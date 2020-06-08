package com.sdm.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthInfo implements UserDetails {
    private String token;
    private int userId;
    private String deviceId;
    private String deviceOs;
    private Date expired;
    Collection<GrantedAuthority> authorities;

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
        return this.deviceOs;
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
