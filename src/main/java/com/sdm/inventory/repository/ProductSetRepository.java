package com.sdm.inventory.repository;

import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.inventory.model.ProductSet;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSetRepository extends DefaultRepository<ProductSet, Integer> {
}
