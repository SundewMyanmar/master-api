package com.sdm.inventory.controller;

import com.sdm.core.controller.DefaultReadWriteController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.inventory.model.Attribute;
import com.sdm.inventory.repository.AttributeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/inventory/attributes")
public class AttributeController extends DefaultReadWriteController<Attribute, Integer> {
    @Autowired
    private AttributeRepository repository;

    @Override
    protected DefaultRepository<Attribute, Integer> getRepository() {
        return this.repository;
    }
}
