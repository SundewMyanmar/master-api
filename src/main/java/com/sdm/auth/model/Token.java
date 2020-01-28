package com.sdm.auth.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sdm.admin.model.User;
import com.sdm.core.model.DefaultEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Audited
@Entity(name = "auth.TokenEntity")
@Table(name = "tbl_auth_tokens")
@JsonPropertyOrder(value = {"deviceId", "deviceOs", "tokenExpired"})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Token extends DefaultEntity implements Serializable {

    private static final long serialVersionUID = -7999643701327132659L;

    @Id
    @Column(unique = true, nullable = false, columnDefinition = "CHAR(36)", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userId")
    @NotFound(action = NotFoundAction.IGNORE)
    private User user;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, columnDefinition = "VARCHAR(255)", length = 255)
    private String deviceId;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)", length = 50)
    private String deviceOs;

    @Column(length = 500)
    private String firebaseToken;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date lastLogin;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date tokenExpired;

    @Override
    public String getId() {
        return id;
    }
}
