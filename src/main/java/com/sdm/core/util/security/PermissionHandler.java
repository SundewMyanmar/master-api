package com.sdm.core.util.security;

import java.util.List;

public interface PermissionHandler {
    List<PermissionMatcher> loadPermissions();
}
