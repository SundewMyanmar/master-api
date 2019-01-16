package com.sdm.core.security;

import com.sdm.master.entity.RoleEntity;
import org.springframework.http.HttpMethod;

import javax.validation.constraints.NotBlank;
import java.util.Set;

public interface PermissionMatcher {
    @NotBlank
    String getPattern();

    HttpMethod getMethod();

    Set<RoleEntity> getRoles();
}
