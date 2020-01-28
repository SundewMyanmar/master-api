package com.sdm.core.controller;

import com.sdm.core.db.DefaultRepository;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.AuthInfo;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.ModelInfo;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.PaginationResponse;
import com.sdm.core.util.CsvManager;
import com.sdm.core.util.Globalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class DefaultReadController<T extends DefaultEntity, ID extends Serializable> implements ReadController<T, ID> {

    @Autowired
    private CsvManager<T> csvManager;

    protected static final Logger logger = LoggerFactory.getLogger(ReadController.class);

    protected abstract DefaultRepository<T, ID> getRepository();

    protected Class<T> getEntityClass() {
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        return (Class<T>) type.getActualTypeArguments()[0];
    }

    protected AuthInfo getCurrentUser() {
        return (AuthInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    protected T checkData(ID id) {
        return this.getRepository().findById(id)
                .orElseThrow(() -> new GeneralException(HttpStatus.NO_CONTENT,
                        "There is no any data by : " + id.toString()));
    }

    protected Pageable buildPagination(int pageId, int pageSize, String sortString) {
        List<Sort.Order> sorting = new ArrayList<>();
        if (sortString.length() > 0) {
            String[] sorts = sortString.split(",");
            for (String sort : sorts) {
                String[] sortParams = sort.trim().split(":", 2);
                if (sortParams.length >= 2 && sortParams[1].equalsIgnoreCase("desc")) {
                    sorting.add(Sort.Order.desc(sortParams[0]));
                } else {
                    sorting.add(Sort.Order.asc(sortParams[0]));
                }
            }
        }
        return PageRequest.of(pageId, pageSize, Sort.by(sorting));
    }

    @Override
    public ResponseEntity<PaginationResponse<T>> getPagingByFilter(int page, int pageSize, String filter, String sort) {
        Page<T> paging = getRepository().findAll(this.buildPagination(page, pageSize, sort), filter);
        PaginationResponse<T> response = new PaginationResponse<>(paging);

        return new ResponseEntity<>(response, HttpStatus.PARTIAL_CONTENT);
    }

    @Override
    public ResponseEntity<ListResponse<T>> getAll() {
        List<T> data = getRepository().findAll();
        return ResponseEntity.ok(new ListResponse<>(data));
    }

    @Override
    public ResponseEntity<T> getById(ID id) {
        T entity = this.checkData(id);
        return ResponseEntity.ok(entity);
    }

    @Override
    public ResponseEntity<Resource> exportByCsv() {
        List<T> data = getRepository().findAll();
        CacheControl cacheControl = CacheControl.maxAge(7, TimeUnit.DAYS);
        String attachment = "attachment; filename=\"" + this.getEntityClass().getName() +
                Globalizer.getDateString("yyyy-MM-dd", new Date()) + ".csv\"";
        try {
            Resource outputResource = csvManager.parseEntityToCsv(data, this.getEntityClass());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, attachment)
                    .cacheControl(cacheControl)
                    .body(outputResource);
        } catch (IllegalAccessException | IOException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    @Override
    public ResponseEntity<ModelInfo> getStructure() {
        throw new GeneralException(HttpStatus.SERVICE_UNAVAILABLE, "Sorry! This services is not available now.");
    }
}
