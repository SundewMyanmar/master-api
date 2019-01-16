package com.sdm.core.security;

import java.util.List;

public interface PermissionHandler {
    List<PermissionMatcher> loadPermissions();
}
