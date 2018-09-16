/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.master.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.security.model.PermissionMatcher;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.http.HttpMethod;

import javax.persistence.*;

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

    @JsonIgnore
    @Formula(value = "concat(pattern, method)")
    private String search;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String pattern;

    @Column
    private String httpMethod;

    @Column
    private boolean everyone;

    @Column
    private boolean user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private RoleEntity role;

    public PermissionEntity() {
    }

    public PermissionEntity(String pattern) {
        this.pattern = pattern;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public int getId() {
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

    public void setEveryone(boolean everyone) {
        this.everyone = everyone;
    }

    public void setUser(boolean user) {
        this.user = user;
    }

    public void setRole(RoleEntity role) {
        this.role = role;
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
    public String getRole() {
        if (this.role == null) {
            return null;
        }

        return this.role.getName();
    }

    @Override
    public boolean isEveryone() {
        return this.everyone;
    }

    @Override
    public boolean isUser() {
        return this.user;
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
