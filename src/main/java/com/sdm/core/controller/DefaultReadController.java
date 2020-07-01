package com.sdm.core.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.Constants;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.AuthInfo;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.ModelInfo;
import com.sdm.core.model.SundewAuditEntity;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.model.response.PaginationResponse;
import com.sdm.core.repository.DefaultRepository;
import com.sdm.core.service.StructureService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.log4j.Log4j2;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Log4j2
public abstract class DefaultReadController<T extends DefaultEntity, ID extends Serializable> implements ReadController<T, ID> {

    protected abstract DefaultRepository<T, ID> getRepository();

    @Autowired
    protected StructureService structureService;

    @Autowired
    protected AuditReader auditReader;

    @Autowired
    ObjectMapper objectMapper;

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

    @ApiOperation(value = "Get Histories", notes = "Get all histories of data.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Permission Denied.", response = MessageResponse.class),
            @ApiResponse(code = 403, message = "Access Forbidden.", response = MessageResponse.class),
            @ApiResponse(code = 404, message = "URL Not Found.", response = MessageResponse.class),
            @ApiResponse(code = 500, message = "Server Error.", response = MessageResponse.class),
    })
    @GetMapping(value = "/{id}/histories", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ListResponse<Map<String, Object>>> getAuditHistory(@PathVariable(value = "id", required = true) ID id) {
        List<SundewAuditEntity> audits = auditReader.createQuery()
                .forRevisionsOfEntity(getEntityClass(), true)
                .add(AuditEntity.id().eq(id))
                .addOrder(AuditEntity.revisionNumber().desc())
                .setMaxResults(10)
                .getResultList();

        List<Map<String, Object>> histories = audits.stream()
                .map((SundewAuditEntity audit) -> {
                    T data = auditReader.find(getEntityClass(), id, audit.getId());
                    Map<String, Object> history = objectMapper.convertValue(data, new TypeReference<Map<String, Object>>() {
                    });
                    history.put("auditInfo", audit);
                    history.remove("version");
                    return history;
                }).collect(Collectors.toList());

        return ResponseEntity.ok(new ListResponse<>(histories));
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
    public ResponseEntity<ListResponse<ModelInfo>> getStructure() {
        var structure = structureService.buildStructure(this.getEntityClass());
        CacheControl cacheControl = CacheControl.maxAge(Duration.ofDays(Constants.STRUCT_CACHE_DAYS));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .cacheControl(cacheControl)
                .body(new ListResponse<>(structure));
    }
}
