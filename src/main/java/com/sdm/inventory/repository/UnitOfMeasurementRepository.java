package com.sdm.inventory.repository;

import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.inventory.model.UnitOfMeasurement;

import org.springframework.stereotype.Repository;

@Repository
public interface UnitOfMeasurementRepository extends DefaultRepository<UnitOfMeasurement, Integer> {
}
