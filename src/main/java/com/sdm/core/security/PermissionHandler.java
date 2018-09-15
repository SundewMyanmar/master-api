package com.sdm.core.security;

import com.sdm.core.security.model.PermissionMatcher;

import java.util.List;

public interface PermissionHandler {
    List<PermissionMatcher> getAllMatchers();
}
