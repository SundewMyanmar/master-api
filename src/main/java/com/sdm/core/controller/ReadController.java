package com.sdm.core.controller;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.response.PaginationModel;
import com.sdm.core.util.Globalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public abstract class ReadController<T extends DefaultEntity, ID extends Serializable> {

    protected static final Logger logger = LoggerFactory.getLogger(ReadController.class);

    protected abstract JpaRepository<T, ID> getRepository();

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

    @PostMapping("q")
    ResponseEntity advanceSearch() {
        throw new GeneralException(HttpStatus.SERVICE_UNAVAILABLE,
            "It is not avaialable now. We are developing.");
    }

    @GetMapping("/")
    ResponseEntity getPageByPage(@RequestParam(value = "page", defaultValue = "0") int pageId,
                                 @RequestParam(value = "size", defaultValue = "10") int pageSize,
                                 @RequestParam(value = "sort", defaultValue = "id:DESC") String sortString) {
        try {
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
            Page<T> paging = getRepository().findAll(this.buildPagination(pageId, pageSize, sortString));
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
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        Class<T> entityClass = (Class<T>) type.getActualTypeArguments()[0];
        JsonSchemaGenerator schemaGenerator = new JsonSchemaGenerator(Globalizer.jsonMapper());
        try {
            JsonSchema schema = schemaGenerator.generateSchema(entityClass);
            return ResponseEntity.ok(schema);
        } catch (JsonMappingException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }
}
