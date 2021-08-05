package com.sdm.inventory.controller;

import com.sdm.core.controller.DefaultReadWriteController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.inventory.model.Category;
import com.sdm.inventory.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/inventory/categories")
public class CategoryController extends DefaultReadWriteController<Category, Integer> {
    @Autowired
    private CategoryRepository repository;

    @Override
    protected DefaultRepository<Category, Integer> getRepository() {
        return this.repository;
    }
}
