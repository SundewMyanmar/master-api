/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.master.entity;

import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Filterable;
import com.sdm.core.security.PermissionMatcher;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.http.HttpMethod;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Htoonlin
 */
@Entity(name = "PermissionEntity")
@Table(name = "tbl_permissions")
public class PermissionEntity extends DefaultEntity implements PermissionMatcher {

    /**
     *
     */
    private static final long serialVersionUID = 231291254158536747L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Filterable
    @Column(nullable = false)
    private String pattern;

    @Filterable
    @Column
    private String httpMethod;

    @NotFound(action = NotFoundAction.IGNORE)
    @JoinTable(name = "tbl_route_permissions",
        joinColumns = {@JoinColumn(name = "route_id")},
        inverseJoinColumns = {@JoinColumn(name = "role_id")})
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<RoleEntity> roles;

    public PermissionEntity() {
    }

    public PermissionEntity(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }

    @Override
    public String getPattern() {
        return this.pattern;
    }

    @Override
    public HttpMethod getMethod() {
        if (this.httpMethod == null) {
            return null;
        }

        return HttpMethod.valueOf(this.httpMethod);
    }

    @Override
    public Set<RoleEntity> getRoles() {
        if (this.roles == null) {
            return new HashSet<>();
        }
        return roles;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (int) (this.id ^ (this.id >>> 32));
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
        final PermissionEntity other = (PermissionEntity) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
}
