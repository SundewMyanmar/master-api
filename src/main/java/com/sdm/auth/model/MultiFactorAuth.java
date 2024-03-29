package com.sdm.auth.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sdm.core.Constants;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.util.Globalizer;

import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "auth.MfaEntity")
@Table(name = "tbl_auth_mfa_settings")
@Where(clause = "deleted_at IS NULL")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MultiFactorAuth extends DefaultEntity implements Serializable {

    @Id
    @Column(unique = true, nullable = false, columnDefinition = "CHAR(36)", length = 36)
    private String id;

    @JsonIgnore
    @Column(nullable = false)
    private int userId;

    @JsonIgnore
    @Column(length = 32)
    private String secret;

    @JsonIgnore
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date secretExpire;

    @JsonIgnore
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean verify;

    @JsonIgnore
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean main;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @NotBlank
    @NotNull
    @Size(max = 255)
    @Column
    private String mfaKey;

    public MultiFactorAuth(MultiFactorAuth mfa) {
        this.id = UUID.randomUUID().toString();
        this.userId = mfa.userId;
        this.mfaKey = mfa.mfaKey;
        this.main = mfa.main;

        if (Globalizer.isNullOrEmpty(mfa.mfaKey)) {
            this.type = Type.APP;
        } else if (Globalizer.isEmail(mfa.mfaKey)) {
            this.type = Type.EMAIL;
        } else if (Globalizer.isPhoneNo(mfa.mfaKey)) {
            this.type = Type.SMS;
        } else {
            this.type = mfa.type;
        }
    }

    public MultiFactorAuth(int userId) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.mfaKey = Constants.APP_NAME;
        this.type = Type.APP;
    }

    @JsonGetter("default")
    public boolean isMain() {
        return this.main;
    }

    public enum Type {
        EMAIL(180),
        SMS(180),
        APP(30);

        private final int life;

        Type(int life) {
            this.life = life;
        }

        public int life() {
            return this.life;
        }
    }

}
