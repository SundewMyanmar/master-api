package com.sdm.core.controller;

import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface ReadWriteController<T, ID extends Serializable> extends ReadController<T, ID> {

    @Operation(summary = "Create New Data", description = "Create new data by JSON Object.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully Created."),
            @ApiResponse(responseCode = "400", description = "Invalid Parameter."),
            @ApiResponse(responseCode = "401", description = "Permission Denied."),
            @ApiResponse(responseCode = "403", description = "Access Forbidden."),
            @ApiResponse(responseCode = "404", description = "URL Not Found."),
            @ApiResponse(responseCode = "406", description = "Unauthorize Token"),
            @ApiResponse(responseCode = "500", description = "Server Error."),
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<T> create(@Valid @RequestBody T body);

    @Operation(summary = "Modified Data", description = "Modified data by JSON Object and Unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "Not Found Data."),
            @ApiResponse(responseCode = "400", description = "Invalid Parameter."),
            @ApiResponse(responseCode = "401", description = "Permission Denied."),
            @ApiResponse(responseCode = "403", description = "Access Forbidden."),
            @ApiResponse(responseCode = "404", description = "URL Not Found."),
            @ApiResponse(responseCode = "406", description = "Unauthorize Token"),
            @ApiResponse(responseCode = "409", description = "Invalid Request."),
            @ApiResponse(responseCode = "500", description = "Server Error."),
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<T> update(@Valid @RequestBody T body, @PathVariable("id") ID id);

    @Operation(summary = "Partially Modified Data", description = "Partially Modified data by JSON Object and Unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "Not Found Data."),
            @ApiResponse(responseCode = "400", description = "Invalid Parameter."),
            @ApiResponse(responseCode = "401", description = "Permission Denied."),
            @ApiResponse(responseCode = "403", description = "Access Forbidden."),
            @ApiResponse(responseCode = "404", description = "URL Not Found."),
            @ApiResponse(responseCode = "406", description = "Unauthorize Token"),
            @ApiResponse(responseCode = "409", description = "Invalid Request."),
            @ApiResponse(responseCode = "500", description = "Server Error."),
    })
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<T> partialUpdate(@RequestBody Map<String, Object> body, @PathVariable("id") ID id);


    @Operation(summary = "Remove Data", description = "Remove data by Unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "Not Found Data."),
            @ApiResponse(responseCode = "400", description = "Invalid Parameter."),
            @ApiResponse(responseCode = "401", description = "Permission Denied."),
            @ApiResponse(responseCode = "403", description = "Access Forbidden."),
            @ApiResponse(responseCode = "404", description = "URL Not Found."),
            @ApiResponse(responseCode = "406", description = "Unauthorize Token"),
            @ApiResponse(responseCode = "409", description = "Invalid Request."),
            @ApiResponse(responseCode = "500", description = "Server Error."),
    })
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<MessageResponse> remove(@PathVariable("id") ID id);


    @Operation(summary = "Remove Multi Data", description = "Remove data by JSON Array.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "Not Found Data."),
            @ApiResponse(responseCode = "400", description = "Invalid Parameter."),
            @ApiResponse(responseCode = "401", description = "Permission Denied."),
            @ApiResponse(responseCode = "403", description = "Access Forbidden."),
            @ApiResponse(responseCode = "404", description = "URL Not Found."),
            @ApiResponse(responseCode = "406", description = "Unauthorize Token"),
            @ApiResponse(responseCode = "409", description = "Invalid Request."),
            @ApiResponse(responseCode = "500", description = "Server Error."),
    })
    @DeleteMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<MessageResponse> multiRemove(@Valid @RequestBody List<ID> ids);

    @Operation(summary = "Data Import", description = "Create, Modified data by data list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "Not Found Data."),
            @ApiResponse(responseCode = "400", description = "Invalid Parameter."),
            @ApiResponse(responseCode = "401", description = "Permission Denied."),
            @ApiResponse(responseCode = "403", description = "Access Forbidden."),
            @ApiResponse(responseCode = "404", description = "URL Not Found."),
            @ApiResponse(responseCode = "406", description = "Unauthorize Token"),
            @ApiResponse(responseCode = "409", description = "Invalid Request."),
            @ApiResponse(responseCode = "500", description = "Server Error."),
    })
    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ListResponse<T>> importData(@Valid @RequestBody List<T> body);
}
