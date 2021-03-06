package com.sdm.core.security;

import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;

public interface PermissionHandler {
    boolean check(Authentication authentication, HttpServletRequest request);
}
