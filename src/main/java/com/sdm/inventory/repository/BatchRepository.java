package com.sdm.inventory.repository;

import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.inventory.model.Batch;

import org.springframework.stereotype.Repository;

@Repository
public interface BatchRepository extends DefaultRepository<Batch, Long> {
}
