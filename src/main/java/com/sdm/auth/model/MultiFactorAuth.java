package com.sdm.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sdm.Constants;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.util.Globalizer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity(name = "auth.MfaEntity")
@Table(name = "tbl_auth_mfa_settings")
@Where(clause = "deleted_at IS NULL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MultiFactorAuth extends DefaultEntity implements Serializable {

    @JsonIgnore
    @Id
    @Column(unique = true, nullable = false, columnDefinition = "CHAR(36)", length = 36)
    private String id;
    @JsonIgnore
    @Column(nullable = false)
    private int userId;
    @JsonIgnore
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;
    @JsonIgnore
    @Column(nullable = false, length = 32)
    private String secret;
    @JsonIgnore
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date secretExpire;
    @JsonIgnore
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean verify;
    @NotBlank
    @NotNull
    @Size(max = 255)
    @Column(nullable = false)
    private String key;
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean totp;
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean main;

    public MultiFactorAuth(MultiFactorAuth mfa) {
        this.id = UUID.randomUUID().toString();
        this.userId = mfa.userId;
        this.key = mfa.key;
        this.totp = mfa.totp;
        this.main = mfa.main;

        if (Globalizer.isEmail(mfa.key)) {
            this.type = Type.EMAIL;
        } else if (Globalizer.isPhoneNo(mfa.key)) {
            this.type = Type.SMS;
        } else {
            this.type = Type.APP;
            this.totp = true;
        }
    }

    public MultiFactorAuth(int userId) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.key = Constants.APP_NAME;
        this.type = Type.APP;
        this.totp = true;
    }

    public enum Type {
        EMAIL(180),
        SMS(180),
        APP(30);

        private int life;

        Type(int life) {
            this.life = life;
        }

        public int life() {
            return this.life;
        }
    }

}
