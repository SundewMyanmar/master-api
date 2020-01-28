package com.sdm.core.controller;

import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.ModelInfo;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.model.response.PaginationResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;

public interface ReadController<T extends DefaultEntity, ID extends Serializable> {

    @ApiOperation(value = "GetAll by Paging & Filter", notes = "Retrieve all data by pagination and filter.")
    @ApiResponses({
            @ApiResponse(code = 401, message = "Permission Denied.", response = MessageResponse.class),
            @ApiResponse(code = 403, message = "Access Forbidden.", response = MessageResponse.class),
            @ApiResponse(code = 404, message = "URL Not Found.", response = MessageResponse.class),
            @ApiResponse(code = 500, message = "Server Error.", response = MessageResponse.class),
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaginationResponse<T>> getPagingByFilter(@RequestParam(value = "page", defaultValue = "0") int page,
                                                            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                                            @RequestParam(value = "filter", defaultValue = "") String filter,
                                                            @RequestParam(value = "sort", defaultValue = "id:DESC") String sort);

    @ApiOperation(value = "GetAll Data", notes = "Retrieve all data without pagination and filter.")
    @ApiResponses({
            @ApiResponse(code = 401, message = "Permission Denied.", response = MessageResponse.class),
            @ApiResponse(code = 403, message = "Access Forbidden.", response = MessageResponse.class),
            @ApiResponse(code = 404, message = "URL Not Found.", response = MessageResponse.class),
            @ApiResponse(code = 500, message = "Server Error.", response = MessageResponse.class),
    })
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ListResponse<T>> getAll();


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


    @ApiOperation(value = "Export Data by CSV", notes = "Export all data to a CSV file.")
    @ApiResponses({
            @ApiResponse(code = 401, message = "Permission Denied.", response = MessageResponse.class),
            @ApiResponse(code = 403, message = "Access Forbidden.", response = MessageResponse.class),
            @ApiResponse(code = 404, message = "URL Not Found.", response = MessageResponse.class),
            @ApiResponse(code = 500, message = "Server Error.", response = MessageResponse.class),
    })
    @GetMapping(value = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<Resource> exportByCsv();

    @ApiOperation(value = "Model Structure", notes = "Model structure to generate UI.")
    @ApiResponses({
            @ApiResponse(code = 401, message = "Permission Denied.", response = MessageResponse.class),
            @ApiResponse(code = 403, message = "Access Forbidden.", response = MessageResponse.class),
            @ApiResponse(code = 404, message = "URL Not Found.", response = MessageResponse.class),
            @ApiResponse(code = 500, message = "Server Error.", response = MessageResponse.class),
    })
    @GetMapping(value = "/struct", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ModelInfo> getStructure();
}
