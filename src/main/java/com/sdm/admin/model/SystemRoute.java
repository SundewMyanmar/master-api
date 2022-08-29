/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sdm.core.Constants;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Searchable;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Htoonlin
 */
@Audited
@Entity(name = "admin.SystemRouteEntity")
@Table(name = "tbl_admin_system_routes")
@Where(clause = "deleted_at IS NULL")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SystemRoute extends DefaultEntity implements Comparable<SystemRoute> {

    /**
     *
     */
    private static final long serialVersionUID = 231291254158536747L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Searchable
    @Column(nullable = false)
    private String pattern;

    @Searchable
    @Column(nullable = false)
    private String module;

    @Searchable
    @Column(nullable = false)
    private String httpMethod;

    @NotAudited
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinTable(name = "tbl_admin_system_route_permissions",
            joinColumns = {@JoinColumn(name = "routeId")},
            inverseJoinColumns = {@JoinColumn(name = "roleId")})
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> allowRoles;

    @Override
    public Integer getId() {
        return id;
    }

    @JsonIgnore
    public String getSqlPattern() {
        return this.pattern.replaceAll("\\{[^}/]+}", "%");
    }

    public void addRole(Role role) {
        if (this.allowRoles == null) {
            this.allowRoles = new HashSet<>();
        }

        this.allowRoles.add(role);
    }

    public boolean checkPermission(GrantedAuthority authority) {
        return this.allowRoles.stream().anyMatch(r -> (Constants.Auth.AUTHORITY_PREFIX + r.getId()).equals(authority.getAuthority()));
    }


    /**
     * Need to sort for Route Permission
     *
     * @return
     */
    public Long getPercentCount() {
        return getSqlPattern().chars().filter(c -> c == '%').count();
    }

    @Override
    public int compareTo(SystemRoute o) {
        if (getSqlPattern() == null || o.getSqlPattern() == null) {
            return 0;
        }
        return getPercentCount().compareTo(o.getPercentCount());
    }
}
