package com.sdm.inventory.controller;

import com.sdm.core.controller.DefaultReadWriteController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.inventory.model.UnitOfMeasurement;
import com.sdm.inventory.repository.UnitOfMeasurementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/inventory/uoms")
public class UnitOfMeasurementController extends DefaultReadWriteController<UnitOfMeasurement, Integer> {
    @Autowired
    private UnitOfMeasurementRepository repository;

    @Override
    protected DefaultRepository<UnitOfMeasurement, Integer> getRepository() {
        return this.repository;
    }
}
