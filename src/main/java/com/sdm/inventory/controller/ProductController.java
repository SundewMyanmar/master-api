package com.sdm.inventory.controller;

import com.sdm.core.controller.DefaultReadWriteController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.inventory.model.Product;
import com.sdm.inventory.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Controller
@RequestMapping("/inventory/products")
public class ProductController extends DefaultReadWriteController<Product, Integer> {

    @Autowired
    private ProductRepository repository;

    @Override
    protected DefaultRepository<Product, Integer> getRepository() {
        return this.repository;
    }

    @GetMapping("/barcodes")
    ResponseEntity<List<String>> getBarcodeTypes() {
        List<String> result = new ArrayList<>();
        EnumSet.allOf(Product.BarcodeType.class)
                .forEach(type -> result.add(type.toString()));
        return ResponseEntity.ok(result);
    }
}
