package com.sdm.master.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.MyanmarFontManager;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.*;

@Entity(name = "UserEntity")
@Table(name = "tbl_users")
public class UserEntity extends DefaultEntity implements Serializable {

    public enum Status {
        ACTIVE,
        PENDING,
        CANCEL
    }

    /**
     *
     */
    private static final long serialVersionUID = 1939600458371706458L;
    public static final int TOKEN_LENGTH = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    @Size(min = 4, max = 255)
    @Column(name = "userName", unique = true, nullable = false)
    private String userName;

    @Email
    @Size(max = 255)
    @Column(name = "email", unique = true)
    private String email;

    @Size(max = 255)
    @JsonIgnore
    @Column(name = "display_name")
    private String displayName;

    @NotFound(action = NotFoundAction.IGNORE)
    @JoinTable(name = "tbl_user_roles",
        joinColumns = {@JoinColumn(name = "user_id")},
        inverseJoinColumns = {@JoinColumn(name = "role_id")})
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<RoleEntity> roles = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "tbl_user_extras",
        joinColumns = @JoinColumn(name = "user_id", nullable = false))
    private Map<String, String> extras = new HashMap();

    @NotBlank
    @Size(min = 6, max = 255)
    @Column(name = "password", columnDefinition = "VARCHAR(255)", nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profile_image", nullable = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private FileEntity profileImage;

    @Column(name = "facebook_id", unique = true, nullable = true, columnDefinition = "VARCHAR(255)", length = 500)
    private String facebookId;

    @JsonIgnore
    @Column(name = "otp_token", length = TOKEN_LENGTH)
    private String otpToken;

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "otp_expired", length = 19)
    private Date otpExpired;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Transient
    private String currentToken;

    public UserEntity() {
    }

    public UserEntity(String email, String userName, String displayName, String password, Status status) {
        this.email = email;
        this.userName = userName;
        this.displayName = displayName;
        this.password = password;
        this.status = status;
    }

    public UserEntity(String userName, String displayName, String password, Status status) {
        this.userName = userName;
        this.displayName = displayName;
        this.password = password;
        this.status = status;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean hasEmail() {
        return this.email != null && this.email.length() > 3 && Globalizer.isEmail(this.email);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @JsonGetter("display_name")
    public Object getMMDisplayName() {
        return MyanmarFontManager.getResponseObject(this.displayName);
    }

    @JsonSetter("display_name")
    public void setMMDisplayName(String displayName) {
        this.displayName = MyanmarFontManager.toUnicode(displayName);
    }

    public Set<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }

    public void addRole(RoleEntity role) {
        this.roles.add(role);
    }

    @JsonIgnore
    public String getPassword() {
        return this.password;
    }

    @JsonSetter("password")
    public void setPassword(String password) {
        this.password = password;
    }

    public FileEntity getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(FileEntity profileImage) {
        this.profileImage = profileImage;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookID) {
        this.facebookId = facebookID;
    }

    public String getOtpToken() {
        return this.otpToken;
    }

    public void setOtpToken(String otpToken) {
        this.otpToken = otpToken;
    }

    public Date getOtpExpired() {
        return this.otpExpired;
    }

    public void setOtpExpired(Date otpExpired) {
        this.otpExpired = otpExpired;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Map<String, String> getExtras() {
        return extras;
    }

    public void setExtras(Map<String, String> extras) {
        this.extras = extras;
    }

    public void addExtra(String key, String value) {
        if (this.extras == null) {
            this.extras = new HashMap<>();
        }

        this.extras.put(key, value);
    }

    public String getCurrentToken() {
        return currentToken;
    }

    public void setCurrentToken(String currentToken) {
        this.currentToken = currentToken;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserEntity other = (UserEntity) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

}
