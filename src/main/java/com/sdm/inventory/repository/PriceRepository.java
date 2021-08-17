package com.sdm.inventory.repository;

import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.inventory.model.Price;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceRepository extends DefaultRepository<Price, Long> {
}
