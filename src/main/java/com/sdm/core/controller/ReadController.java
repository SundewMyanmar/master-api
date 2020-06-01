package com.sdm.core.controller;

import com.sdm.core.model.ModelInfo;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.PaginationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

public interface ReadController<T, ID extends Serializable> {

    @Operation(summary = "GetAll by Paging & Filter", description = "Retrieve all data by pagination and filter.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Permission Denied."),
            @ApiResponse(responseCode = "403", description = "Access Forbidden."),
            @ApiResponse(responseCode = "404", description = "URL Not Found."),
            @ApiResponse(responseCode = "406", description = "Unauthorize Token"),
            @ApiResponse(responseCode = "500", description = "Server Error."),
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaginationResponse<T>> getPagingByFilter(
            @Parameter(description = "Page Index No.", required = true, in = ParameterIn.QUERY) @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Records count per page.", required = true, in = ParameterIn.QUERY) @RequestParam(value = "size", defaultValue = "10") int pageSize,
            @Parameter(description = "Global filter value", required = true, in = ParameterIn.QUERY) @RequestParam(value = "filter", defaultValue = "") String filter,
            @Parameter(description = "Sortable info; Eg. sort=id:Desc", required = true, in = ParameterIn.QUERY) @RequestParam(value = "sort", defaultValue = "id:DESC") String sort);


    @Operation(summary = "GetAll Data", description = "Retrieve all data by async.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Permission Denied."),
            @ApiResponse(responseCode = "403", description = "Access Forbidden."),
            @ApiResponse(responseCode = "404", description = "URL Not Found."),
            @ApiResponse(responseCode = "406", description = "Unauthorize Token"),
            @ApiResponse(responseCode = "500", description = "Server Error."),
    })
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    CompletableFuture<ResponseEntity<ListResponse<T>>> getAll();


    @Operation(summary = "Get Data by Unique ID", description = "Retrieve data by Unique ID/DB Primary Key.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Not Found Data."),
            @ApiResponse(responseCode = "401", description = "Permission Denied."),
            @ApiResponse(responseCode = "403", description = "Access Forbidden."),
            @ApiResponse(responseCode = "404", description = "URL Not Found."),
            @ApiResponse(responseCode = "406", description = "Unauthorize Token"),
            @ApiResponse(responseCode = "500", description = "Server Error."),
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<T> getById(@Parameter(description = "Unique ID of Data", required = true, in = ParameterIn.PATH) @PathVariable("id") ID id);


    @Operation(summary = "Model Structure", description = "Model structure to generate UI.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Permission Denied."),
            @ApiResponse(responseCode = "403", description = "Access Forbidden."),
            @ApiResponse(responseCode = "404", description = "URL Not Found."),
            @ApiResponse(responseCode = "406", description = "Unauthorize Token"),
            @ApiResponse(responseCode = "500", description = "Server Error."),
    })
    @GetMapping(value = "/struct", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ListResponse<ModelInfo>> getStructure();
}
