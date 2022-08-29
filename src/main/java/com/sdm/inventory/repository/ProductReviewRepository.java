package com.sdm.inventory.repository;

import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.inventory.model.ProductReview;

import org.springframework.stereotype.Repository;

@Repository
public interface ProductReviewRepository extends DefaultRepository<ProductReview, String> {
}
