package com.sdm.admin.controller;

import com.sdm.admin.model.Role;
import com.sdm.admin.repository.RoleRepository;
import com.sdm.core.controller.DefaultReadWriteController;
import com.sdm.core.repository.DefaultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/roles")
public class RoleController extends DefaultReadWriteController<Role, Integer> {
    @Autowired
    private RoleRepository repository;

    @Override
    protected DefaultRepository<Role, Integer> getRepository() {
        return repository;
    }
}
