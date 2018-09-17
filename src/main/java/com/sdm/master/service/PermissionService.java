package com.sdm.master.service;

import com.sdm.core.security.PermissionHandler;
import com.sdm.core.security.PermissionMatcher;
import com.sdm.master.entity.PermissionEntity;
import com.sdm.master.entity.RoleEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("permissionHandler")
public class PermissionService implements PermissionHandler {

    @Override
    public List<PermissionMatcher> loadPermissions() {
        PermissionEntity testPermission = new PermissionEntity();
        testPermission.setPattern("/role/**");
        testPermission.setRole(new RoleEntity("admin"));
        List<PermissionMatcher> permissions = new ArrayList<>();
        permissions.add(testPermission);
        return permissions;
    }
}
