package com.sdm.core.security.model;

import org.springframework.http.HttpMethod;

public interface PermissionMatcher {

    String getPattern();

    HttpMethod getMethod();

    String getRole();

    boolean isEveryone();

    boolean isUser();
}
