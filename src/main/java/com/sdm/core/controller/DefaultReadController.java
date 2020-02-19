package com.sdm.core.controller;

import com.sdm.core.db.DefaultRepository;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.AuthInfo;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.ModelInfo;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.PaginationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.transaction.Transactional;

public abstract class DefaultReadController<T extends DefaultEntity, ID extends Serializable> implements ReadController<T, ID> {

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
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE,
                        "There is no any data by : " + id.toString()));
    }

    protected Pageable buildPagination(int pageId, int pageSize, String sortString) {
        sortString = sortString.replaceAll("\\s+", "");
        List<Sort.Order> sorting = new ArrayList<>();
        if (!StringUtils.isEmpty(sortString)) {
            String[] sorts = sortString.split(",");
            for (String sort : sorts) {
                String[] sortParams = sort.strip().split(":", 2);
                if (sortParams.length >= 2 && sortParams[1].equalsIgnoreCase("desc")) {
                    sorting.add(Sort.Order.desc(sortParams[0]));
                } else {
                    sorting.add(Sort.Order.asc(sortParams[0]));
                }
            }
        }
        return PageRequest.of(pageId, pageSize, Sort.by(sorting));
    }

    @Transactional
    @Override
    public ResponseEntity<PaginationResponse<T>> getPagingByFilter(int page, int pageSize, String filter, String sort) {
        Page<T> paging = getRepository().findAll(filter, this.buildPagination(page, pageSize, sort));
        PaginationResponse<T> response = new PaginationResponse<>(paging);

        return new ResponseEntity<>(response, HttpStatus.PARTIAL_CONTENT);
    }

    @Async
    @Override
    public CompletableFuture<ResponseEntity<ListResponse<T>>> getAll() {
        List<T> data = getRepository().findAll();
        ResponseEntity<ListResponse<T>> response = ResponseEntity.ok(new ListResponse<>(data));
        return CompletableFuture.completedFuture(response);
    }

    @Override
    public ResponseEntity<T> getById(ID id) {
        T entity = this.checkData(id);
        return ResponseEntity.ok(entity);
    }

    @Override
    public ResponseEntity<ModelInfo> getStructure() {
        throw new GeneralException(HttpStatus.SERVICE_UNAVAILABLE, "Sorry! This services is not available now.");
    }
}
