package com.sdm.admin.service;

import com.sdm.admin.model.SystemRoute;
import com.sdm.admin.repository.PermissionRepository;
import com.sdm.core.util.security.PermissionHandler;
import com.sdm.core.util.security.PermissionMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("permissionHandler")
public class PermissionService implements PermissionHandler {

    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public List<PermissionMatcher> loadPermissions() {
        List<SystemRoute> systemRoutes = permissionRepository.findAll();
        List<PermissionMatcher> permissionMatchers = new ArrayList<>();

        systemRoutes.forEach(entity -> permissionMatchers.add(entity));
        /*
        PermissionEntity testPermission = new PermissionEntity();
        testPermission.setPattern("/roles/**");
        testPermission.setHttpMethod("GET");
        testPermission.setRole(new RoleEntity("ADMIN"));
        List<PermissionMatcher> permissions = new ArrayList<>();
        permissions.add(testPermission);
        return permissions;*/
        return permissionMatchers;
    }
}