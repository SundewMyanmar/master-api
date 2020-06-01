package com.sdm.core.controller;

import com.sdm.core.model.ModelInfo;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.PaginationResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

public interface ReadController<T, ID extends Serializable> {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaginationResponse<T>> getPagingByFilter(@RequestParam(value = "page", defaultValue = "0") int page,
                                                            @RequestParam(value = "size", defaultValue = "10") int pageSize,
                                                            @RequestParam(value = "filter", defaultValue = "") String filter,
                                                            @RequestParam(value = "sort", defaultValue = "id:DESC") String sort);

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    CompletableFuture<ResponseEntity<ListResponse<T>>> getAll();

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<T> getById(/*@Parameter(description = "Unique ID of Data", required = true, in = ParameterIn.PATH)*/ @PathVariable("id") ID id);

    @GetMapping(value = "/struct", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ListResponse<ModelInfo>> getStructure();
}
