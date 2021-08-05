package com.sdm.inventory.repository;

import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.inventory.model.Attribute;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeRepository extends DefaultRepository<Attribute, Integer> {
}
