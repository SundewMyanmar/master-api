package com.sdm.core.controller;

import com.sdm.core.model.AdvancedFilter;
import com.sdm.core.model.ModelInfo;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.model.response.PaginationResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ReadController<T, ID extends Serializable> {

    @ApiOperation(value = "GetAll by Paging & Filter", notes = "Retrieve all data by pagination and filter.")
    @ApiResponses({
            @ApiResponse(code = 401, message = "Permission Denied.", response = MessageResponse.class),
            @ApiResponse(code = 403, message = "Access Forbidden.", response = MessageResponse.class),
            @ApiResponse(code = 404, message = "URL Not Found.", response = MessageResponse.class),
            @ApiResponse(code = 500, message = "Server Error.", response = MessageResponse.class),
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaginationResponse<T>> getPagingByFilter(@RequestParam(value = "page", defaultValue = "0") int page,
                                                            @RequestParam(value = "size", defaultValue = "10") int pageSize,
                                                            @RequestParam(value = "filter", defaultValue = "") String filter,
                                                            @RequestParam(value = "sort", defaultValue = "modifiedAt:DESC") String sort);

    @ApiOperation(value = "GetAll Data", notes = "Retrieve all data by async.")
    @ApiResponses({
            @ApiResponse(code = 401, message = "Permission Denied.", response = MessageResponse.class),
            @ApiResponse(code = 403, message = "Access Forbidden.", response = MessageResponse.class),
            @ApiResponse(code = 404, message = "URL Not Found.", response = MessageResponse.class),
            @ApiResponse(code = 500, message = "Server Error.", response = MessageResponse.class),
    })
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    CompletableFuture<ResponseEntity<ListResponse<T>>> getAll();

    @ApiOperation(value = "GetAll by Paging & Advanced Filter", notes = "Retrieve all data by pagination and Advanced Filter.")
    @ApiResponses({
            @ApiResponse(code = 401, message = "Permission Denied.", response = MessageResponse.class),
            @ApiResponse(code = 403, message = "Access Forbidden.", response = MessageResponse.class),
            @ApiResponse(code = 404, message = "URL Not Found.", response = MessageResponse.class),
            @ApiResponse(code = 500, message = "Server Error.", response = MessageResponse.class),
    })
    @PostMapping(value = "/advanced", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaginationResponse<T>> getPagingByAdvancedFilter(@Valid @RequestBody List<AdvancedFilter> filters,
                                                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                                                    @RequestParam(value = "size", defaultValue = "10") int pageSize,
                                                                    @RequestParam(value = "sort", defaultValue = "id:DESC") String sort);


    @ApiOperation(value = "Get Data by Unique ID", notes = "Retrieve data by Unique ID/DB Primary Key.")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Not Found Data.", response = MessageResponse.class),
            @ApiResponse(code = 401, message = "Permission Denied.", response = MessageResponse.class),
            @ApiResponse(code = 403, message = "Access Forbidden.", response = MessageResponse.class),
            @ApiResponse(code = 404, message = "URL Not Found.", response = MessageResponse.class),
            @ApiResponse(code = 500, message = "Server Error.", response = MessageResponse.class),
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<T> getById(/*@Parameter(description = "Unique ID of Data", required = true, in = ParameterIn.PATH)*/ @PathVariable("id") ID id);

    @ApiOperation(value = "Get Histories", notes = "Get all histories of data.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Permission Denied.", response = MessageResponse.class),
            @ApiResponse(code = 403, message = "Access Forbidden.", response = MessageResponse.class),
            @ApiResponse(code = 404, message = "URL Not Found.", response = MessageResponse.class),
            @ApiResponse(code = 500, message = "Server Error.", response = MessageResponse.class),
    })
    @GetMapping(value = "/{id}/histories", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ListResponse<Map<String, Object>>> getAuditHistory(@PathVariable(value = "id", required = true) ID id);

    @ApiOperation(value = "Model Structure", notes = "Model structure to generate UI.")
    @ApiResponses({
            @ApiResponse(code = 401, message = "Permission Denied.", response = MessageResponse.class),
            @ApiResponse(code = 403, message = "Access Forbidden.", response = MessageResponse.class),
            @ApiResponse(code = 404, message = "URL Not Found.", response = MessageResponse.class),
            @ApiResponse(code = 500, message = "Server Error.", response = MessageResponse.class),
    })
    @GetMapping(value = "/struct", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ListResponse<ModelInfo>> getStructure();
}