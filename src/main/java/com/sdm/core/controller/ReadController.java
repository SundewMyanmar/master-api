package com.sdm.core.controller;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.sdm.core.component.CsvManager;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.AuthInfo;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Filterable;
import com.sdm.core.model.response.PaginationModel;
import com.sdm.core.repository.DefaultRepository;
import com.sdm.core.util.Globalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class ReadController<T extends DefaultEntity, ID extends Serializable> {
    private CsvManager<T> csvManager;

    protected CsvManager<T> getCSVManager() {
        if (csvManager == null) {
            csvManager = new CsvManager<>();
        }
        return csvManager;
    }

    protected static final Logger logger = LoggerFactory.getLogger(ReadController.class);

    protected abstract DefaultRepository<T, ID> getRepository();

    protected Class<T> getEntityClass() {
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        return (Class<T>) type.getActualTypeArguments()[0];
    }

    protected AuthInfo getCurrentUser() {
        return (AuthInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private List<String> getFilterFields() {
        List<String> fields = new ArrayList();
        Arrays.stream(getEntityClass().getDeclaredFields()).forEach(field ->
                Arrays.stream(field.getDeclaredAnnotations()).forEach(annotation -> {
                    if (annotation.annotationType().equals(Filterable.class)) {
                        fields.add(field.getName());
                    }
                })
        );
        return fields;
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

    @GetMapping()
    ResponseEntity getPageByPage(@RequestParam(value = "page", defaultValue = "0") int pageId,
                                 @RequestParam(value = "size", defaultValue = "10") int pageSize,
                                 @RequestParam(value = "sort", defaultValue = "id:DESC") String sortString,
                                 @RequestParam(value = "filter", defaultValue = "") String filter) {
        try {
            Page<T> paging = getRepository().findAll(this.buildPagination(pageId, pageSize, sortString), filter, this.getFilterFields());
            return new ResponseEntity(new PaginationModel(paging), HttpStatus.PARTIAL_CONTENT);
        } catch (Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
            throw ex;
        }
    }

    @GetMapping("/{id}")
    ResponseEntity getById(@PathVariable("id") ID id) {
        T entity = this.checkData(id);
        return ResponseEntity.ok(entity);
    }

    @GetMapping("/struct")
    ResponseEntity getStructure() {
        JsonSchemaGenerator schemaGenerator = new JsonSchemaGenerator(Globalizer.jsonMapper());
        try {
            JsonSchema schema = schemaGenerator.generateSchema(this.getEntityClass());
            return ResponseEntity.ok(schema);
        } catch (JsonMappingException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    @PostMapping("/export")
    @Transactional
    ResponseEntity exportCsv() throws IOException, IllegalAccessException {
        List<T> datas = getRepository().findAll();
        CacheControl cacheControl = CacheControl.maxAge(7, TimeUnit.DAYS);
        String attachment = "attachment; filename=\"" + this.getEntityClass().getName() +
                Globalizer.getDateString("yyyy-MM-dd", new Date()) + ".csv\"";
        Resource outputResource = this.getCSVManager().parseEntityToCsv(datas, this.getEntityClass());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, attachment)
                .cacheControl(cacheControl)
                .body(outputResource);
    }
}
