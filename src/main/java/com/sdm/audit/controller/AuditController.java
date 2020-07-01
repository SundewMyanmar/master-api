package com.sdm.audit.controller;

import com.sdm.audit.service.AuditService;
import com.sdm.core.controller.ReadController;
import com.sdm.core.model.ModelInfo;
import com.sdm.core.model.SundewAuditEntity;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/audit")
public class AuditController implements ReadController<SundewAuditEntity, Integer> {
    @Autowired
    private AuditService service;

    @Override
    public ResponseEntity<PaginationResponse<SundewAuditEntity>> getPagingByFilter(int page, int pageSize, String filter, String sort) {
        return null;
    }

    @Override
    public CompletableFuture<ResponseEntity<ListResponse<SundewAuditEntity>>> getAll() {
        return null;
    }

    @Override
    public ResponseEntity<SundewAuditEntity> getById(Integer integer) {
        return null;
    }

    @Override
    public ResponseEntity<ListResponse<ModelInfo>> getStructure() {
        return null;
    }
}
