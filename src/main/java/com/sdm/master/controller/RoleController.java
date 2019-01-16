package com.sdm.master.controller;

import com.sdm.core.controller.ReadWriteController;
import com.sdm.master.entity.RoleEntity;
import com.sdm.master.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/roles")
public class RoleController extends ReadWriteController<RoleEntity, Integer> {
    @Autowired
    RoleRepository repository;

    @Override
    protected JpaRepository<RoleEntity, Integer> getRepository() {
        return repository;
    }
}
