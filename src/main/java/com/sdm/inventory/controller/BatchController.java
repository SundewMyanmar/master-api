package com.sdm.inventory.controller;

import com.sdm.core.controller.DefaultReadWriteController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.inventory.model.Batch;
import com.sdm.inventory.repository.BatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/inventory/batches")
public class BatchController extends DefaultReadWriteController<Batch, Long> {

    @Autowired
    private BatchRepository repository;

    @Override
    protected DefaultRepository<Batch, Long> getRepository() {
        return this.repository;
    }
}
