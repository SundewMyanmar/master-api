package com.sdm.inventory.controller;

import com.sdm.core.controller.DefaultReadWriteController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.inventory.model.ProductSet;
import com.sdm.inventory.repository.ProductSetRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/inventory/sets")
public class ProductSetController extends DefaultReadWriteController<ProductSet, Integer> {

    @Autowired
    private ProductSetRepository repository;

    @Override
    protected DefaultRepository<ProductSet, Integer> getRepository() {
        return this.repository;
    }
}
