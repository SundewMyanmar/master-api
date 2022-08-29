package com.sdm.inventory.repository;

import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.inventory.model.Product;

import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends DefaultRepository<Product, Integer> {
}
