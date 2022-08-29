package com.sdm.inventory.controller;

import com.sdm.core.controller.DefaultReadWriteController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.core.model.response.PaginationResponse;
import com.sdm.inventory.model.Category;
import com.sdm.inventory.repository.CategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/inventory/categories")
public class CategoryController extends DefaultReadWriteController<Category, Integer> {
    @Autowired
    private CategoryRepository repository;

    @Override
    protected DefaultRepository<Category, Integer> getRepository() {
        return this.repository;
    }

    @GetMapping("/parent")
    ResponseEntity<PaginationResponse<Category>> getPagingByParent(@RequestParam(value = "page", defaultValue = "0") int page,
                                                                   @RequestParam(value = "size", defaultValue = "10") int pageSize,
                                                                   @RequestParam(value = "filter", defaultValue = "") String filter,
                                                                   @RequestParam(value = "sort", defaultValue = "id:DESC") String sort,
                                                                   @RequestParam(value = "parent", defaultValue = "null") Integer parent) {
        Page<Category> pageResult = repository.getPagingByParent(this.buildPagination(page, pageSize, sort), "%" + filter + "%", parent);
        PaginationResponse<Category> response = new PaginationResponse<>(pageResult);
        return new ResponseEntity<>(response, HttpStatus.PARTIAL_CONTENT);
    }
}
