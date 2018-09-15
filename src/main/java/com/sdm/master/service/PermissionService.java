package com.sdm.master.service;

import com.sdm.core.security.PermissionHandler;
import com.sdm.core.security.model.PermissionMatcher;
import com.sdm.master.entity.PermissionEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("permissionHandler")
public class PermissionService implements PermissionHandler {

    @Override
    public List<PermissionMatcher> getAllMatchers() {
        List<PermissionMatcher> permissions = new ArrayList<>();

        PermissionEntity authPermission = new PermissionEntity("/auth/**");
        authPermission.setEveryone(true);
        permissions.add(authPermission);

        PermissionEntity profilePermission = new PermissionEntity("/me/**");
        profilePermission.setUser(true);
        permissions.add(profilePermission);

        return permissions;
    }
}
