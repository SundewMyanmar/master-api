package com.sdm.master.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sdm.core.model.DefaultEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "TokenEntity")
@Table(name = "tbl_user_tokens")
@JsonPropertyOrder(value = {"token", "device_id", "device_os", "token_expired"})
public class TokenEntity extends DefaultEntity implements Serializable {

    private static final long serialVersionUID = -7999643701327132659L;

    @Id
    @Column(name = "token", unique = true, nullable = false, length = 36)
    private String id;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @NotBlank
    @Size(max = 255)
    @Column(name = "device_id", nullable = false, columnDefinition = "VARCHAR(255)", length = 255)
    private String deviceId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "device_os", nullable = false, columnDefinition = "VARCHAR(50)", length = 50)
    private String deviceOs;

    @Column(name = "firebase_token", nullable = true, columnDefinition = "VARCHAR(500)", length = 500)
    private String firebaseToken;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_login", nullable = false, length = 19, updatable = true)
    private Date lastLogin;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "token_expired", nullable = false, length = 19)
    private Date tokenExpired;

    public TokenEntity() {
    }

    public TokenEntity(String id, long userId, String deviceId, String deviceOs, Date lastLogin, Date tokenExpired) {
        this.id = id;
        this.userId = userId;
        this.deviceId = deviceId;
        this.deviceOs = deviceOs;
        this.lastLogin = lastLogin;
        this.tokenExpired = tokenExpired;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceOs() {
        return this.deviceOs;
    }

    public void setDeviceOs(String deviceOs) {
        this.deviceOs = deviceOs;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }

    public void setFirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }

    public Date getLastLogin() {
        return this.lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Date getTokenExpired() {
        return this.tokenExpired;
    }

    public void setTokenExpired(Date tokenExpired) {
        this.tokenExpired = tokenExpired;
    }

}
