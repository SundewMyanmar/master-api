package com.sdm.core.util.security;

import org.springframework.http.HttpMethod;

import javax.validation.constraints.NotBlank;
import java.util.Set;

public interface PermissionMatcher {
    @NotBlank
    String getPattern();

    HttpMethod getMethod();

    Set<String> getRoles();
}
