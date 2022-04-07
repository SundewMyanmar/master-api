package com.sdm.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.sdm.auth.model.MultiFactorAuth;
import com.sdm.core.model.Contact;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.FileClassification;
import com.sdm.core.model.annotation.Searchable;
import com.sdm.storage.model.File;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.*;

@Audited
@Entity(name = "admin.UserEntity")
@Table(name = "tbl_admin_users")
@Where(clause = "deleted_at IS NULL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User extends DefaultEntity implements Serializable {

    public static final int TOKEN_LENGTH = 8;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profileImage")
    @NotFound(action = NotFoundAction.IGNORE)
    @FileClassification(guild = "USER", isPublic = true)
    private File profileImage;

    @Searchable
    @Size(max = 255)
    @Column
    private String displayName;

    @Searchable
    @NotBlank
    @Size(min = 5, max = 50)
    @Column(length = 50)
    private String phoneNumber;

    @Searchable
    @Email
    @Size(max = 255)
    @Column
    private String email;

    @Searchable
    @Size(max = 50)
    @Column
    private String type;

    @Searchable
    @Size(max = 500)
    @Column(columnDefinition = "varchar(500)")
    private String note;

    @NotAudited
    @NotFound(action = NotFoundAction.IGNORE)
    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "tbl_admin_user_contacts", joinColumns = @JoinColumn(name = "userId"))
    @OrderBy("priority")
    private Set<Contact> contacts = new HashSet<>();

    @NotAudited
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinTable(name = "tbl_admin_user_roles",
            joinColumns = {@JoinColumn(name = "userId")},
            inverseJoinColumns = {@JoinColumn(name = "roleId")})
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();

    @Size(min = 6)
    @Column(length = 500)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @CollectionTable(name = "tbl_admin_user_extras",
            joinColumns = @JoinColumn(name = "userId", nullable = false))
    private Map<String, String> extras = new HashMap<>();

    @Column(unique = true, length = 500)
    private String appleId;

    @Column(unique = true, length = 500)
    private String facebookId;

    @Column(unique = true, length = 500)
    private String googleId;

    @JsonIgnore
    @Column(length = TOKEN_LENGTH)
    private String activateToken;

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column(length = 19)
    private Date activateTokenExpire;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Transient
    private String currentToken;

    @Transient
    private MultiFactorAuth mfa;

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

    @JsonIgnore
    public String getPassword() {
        return this.password;
    }

    @JsonSetter("password")
    public void setPassword(String password) {
        this.password = password;
    }

    public enum Status {
        ACTIVE,
        PENDING,
        CANCEL
    }

}
