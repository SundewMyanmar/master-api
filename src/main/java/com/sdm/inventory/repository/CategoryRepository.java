package com.sdm.inventory.repository;

import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.inventory.model.Category;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends DefaultRepository<Category, Integer> {
}
