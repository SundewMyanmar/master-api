package com.sdm.admin.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Filterable;
import com.sdm.core.util.MyanmarFontManager;
import com.sdm.file.model.File;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.*;

@Audited
@Entity(name = "admin.UserEntity")
@Table(name = "tbl_admin_users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User extends DefaultEntity implements Serializable {

    public enum Status {
        ACTIVE,
        PENDING,
        CANCEL
    }

    public static final int TOKEN_LENGTH = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Filterable
    @NotBlank
    @Size(min = 6, max = 50)
    @Column(length = 50, unique = true, nullable = false)
    private String phoneNumber;

    @Filterable
    @Email
    @Size(max = 255)
    @Column(unique = true)
    private String email;

    @Filterable
    @Size(max = 255)
    @JsonIgnore
    @Column
    private String displayName;

    @NotBlank
    @Size(min = 6)
    @Column(length = 500, nullable = false)
    private String password;

    @NotFound(action = NotFoundAction.IGNORE)
    @JoinTable(name = "tbl_admin_user_roles",
            joinColumns = {@JoinColumn(name = "userId")},
            inverseJoinColumns = {@JoinColumn(name = "roleId")})
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @CollectionTable(name = "tbl_admin_user_extras",
            joinColumns = @JoinColumn(name = "userId", nullable = false))
    private Map<String, String> extras = new HashMap();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profileImage")
    @NotFound(action = NotFoundAction.IGNORE)
    private File profileImage;

    @Column(unique = true, length = 500)
    private String facebookId;

    @JsonIgnore
    @Column(length = TOKEN_LENGTH)
    private String otpToken;

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column(length = 19)
    private Date otpExpired;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Transient
    private String currentToken;

    public User(String email, String phoneNumber, String displayName, String password, Status status) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.displayName = displayName;
        this.password = password;
        this.status = status;
    }

    public User(String phoneNumber, String displayName, String password, Status status) {
        this.phoneNumber = phoneNumber;
        this.displayName = displayName;
        this.password = password;
        this.status = status;
    }

    public void addExtra(String key, String value) {
        if (this.extras == null) {
            this.extras = new HashMap<>();
        }
        this.extras.put(key, value);
    }

    @Override
    public Integer getId() {
        return id;
    }

    @JsonGetter("displayName")
    public Object getMMDisplayName() {
        return MyanmarFontManager.getResponseObject(this.displayName);
    }

    @JsonSetter("displayName")
    public void setMMDisplayName(String displayName) {
        this.displayName = MyanmarFontManager.toUnicode(displayName);
    }

    @JsonIgnore
    public String getPassword() {
        return this.password;
    }

    @JsonSetter("password")
    public void setPassword(String password) {
        this.password = password;
    }

}
