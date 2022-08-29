package com.sdm.inventory.repository;

import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.inventory.model.ProductRelation;

import org.springframework.stereotype.Repository;

@Repository
public interface ProductRelationRepository extends DefaultRepository<ProductRelation, String> {
}
