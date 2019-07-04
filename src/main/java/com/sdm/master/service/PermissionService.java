package com.sdm.master.service;

import com.sdm.core.security.PermissionHandler;
import com.sdm.core.security.PermissionMatcher;
import com.sdm.master.entity.PermissionEntity;
import com.sdm.master.repository.PermissionRepository;
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
        List<PermissionEntity> permissionEntities = permissionRepository.findAll();
        List<PermissionMatcher> permissionMatchers = new ArrayList<>();

        permissionEntities.forEach(entity -> permissionMatchers.add(entity));
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
