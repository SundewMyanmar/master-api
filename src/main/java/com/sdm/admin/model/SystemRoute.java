/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Filterable;
import com.sdm.core.util.security.PermissionMatcher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.springframework.http.HttpMethod;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Htoonlin
 */
@Audited
@Entity(name = "admin.SystemRouteEntity")
@Table(name = "tbl_admin_system_routes")
@Where(clause = "deleted_at IS NULL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemRoute extends DefaultEntity implements PermissionMatcher {

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
    @JoinTable(name = "tbl_admin_system_route_permissions",
            joinColumns = {@JoinColumn(name = "routeId")},
            inverseJoinColumns = {@JoinColumn(name = "roleId")})
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> allowRoles;

    @Transient
    private boolean checked;

    public SystemRoute(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getPattern() {
        return this.pattern;
    }

    @JsonIgnore
    @Override
    public HttpMethod getMethod() {
        if (this.httpMethod == null) {
            return null;
        }

        return HttpMethod.valueOf(this.httpMethod);
    }

    @JsonIgnore
    @Override
    public Set<String> getRoles() {
        if (this.allowRoles == null) {
            return new HashSet<>();
        }
        return allowRoles.stream().map((role -> role.getName())).collect(Collectors.toSet());
    }
}
