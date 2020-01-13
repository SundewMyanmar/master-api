package com.sdm.master.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sdm.core.model.DefaultEntity;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity(name = "TokenEntity")
@Table(name = "tbl_user_tokens")
@JsonPropertyOrder(value = {"token", "device_id", "device_os", "token_expired"})
public class TokenEntity extends DefaultEntity implements Serializable {

    private static final long serialVersionUID = -7999643701327132659L;

    @Id
    @Column(name = "token", unique = true, nullable = false, columnDefinition = "CHAR(36)", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private UserEntity user;

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

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TokenEntity that = (TokenEntity) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
