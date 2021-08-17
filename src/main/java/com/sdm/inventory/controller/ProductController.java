package com.sdm.inventory.controller;

import com.sdm.core.controller.DefaultReadWriteController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.inventory.model.Product;
import com.sdm.inventory.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/inventory/products")
public class ProductController extends DefaultReadWriteController<Product, Integer> {

    @Autowired
    private ProductRepository repository;

    @Override
    protected DefaultRepository<Product, Integer> getRepository() {
        return this.repository;
    }
}
