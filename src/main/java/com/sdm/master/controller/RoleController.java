package com.sdm.master.controller;

import com.sdm.core.controller.ReadWriteController;
import com.sdm.core.model.response.ListModel;
import com.sdm.core.repository.DefaultRepository;
import com.sdm.master.entity.RoleEntity;
import com.sdm.master.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController extends ReadWriteController<RoleEntity, Integer> {
    @Autowired
    RoleRepository repository;

    @Override
    protected DefaultRepository<RoleEntity, Integer> getRepository() {
        return repository;
    }
    
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResponseEntity getAll() {
        try {
            List<RoleEntity> roles = repository.findAll();
            return new ResponseEntity(new ListModel<>(roles), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
            throw ex;
        }
    }
}
