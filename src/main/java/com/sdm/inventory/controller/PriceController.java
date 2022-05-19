package com.sdm.inventory.controller;

import com.sdm.core.controller.DefaultReadWriteController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.inventory.model.Price;
import com.sdm.inventory.repository.PriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/inventory/prices")
public class PriceController extends DefaultReadWriteController<Price, Long> {
    @Autowired
    private PriceRepository repository;

    @Override
    protected DefaultRepository<Price, Long> getRepository() {
        return this.repository;
    }
}
