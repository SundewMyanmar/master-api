package com.sdm.core.controller;

import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.MessageResponse;
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

public interface WriteController<T, ID extends Serializable> {

    @ApiOperation(value = "Create New Data", notes = "Create new data by JSON Object.")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 400, message = "Invalid Parameter.", response = MessageResponse.class),
            @ApiResponse(code = 401, message = "Permission Denied.", response = MessageResponse.class),
            @ApiResponse(code = 403, message = "Access Forbidden.", response = MessageResponse.class),
            @ApiResponse(code = 404, message = "URL Not Found.", response = MessageResponse.class),
            @ApiResponse(code = 406, message = "Unauthorize Token", response = MessageResponse.class),
            @ApiResponse(code = 500, message = "Server Error.", response = MessageResponse.class),
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<T> create(@Valid @RequestBody T body);

    @ApiOperation(value = "Modified Data", notes = "Modified data by JSON Object and Unique ID.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 204, message = "Not Found Data.", response = MessageResponse.class),
            @ApiResponse(code = 400, message = "Invalid Parameter.", response = MessageResponse.class),
            @ApiResponse(code = 401, message = "Permission Denied.", response = MessageResponse.class),
            @ApiResponse(code = 403, message = "Access Forbidden.", response = MessageResponse.class),
            @ApiResponse(code = 404, message = "URL Not Found.", response = MessageResponse.class),
            @ApiResponse(code = 406, message = "Unauthorize Token", response = MessageResponse.class),
            @ApiResponse(code = 409, message = "Invalid Request.", response = MessageResponse.class),
            @ApiResponse(code = 500, message = "Server Error.", response = MessageResponse.class),
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<T> update(@Valid @RequestBody T body, @PathVariable("id") ID id);

    @ApiOperation(value = "Partially Modified Data", notes = "Partially Modified data by JSON Object and Unique ID.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 204, message = "Not Found Data.", response = MessageResponse.class),
            @ApiResponse(code = 400, message = "Invalid Parameter.", response = MessageResponse.class),
            @ApiResponse(code = 401, message = "Permission Denied.", response = MessageResponse.class),
            @ApiResponse(code = 403, message = "Access Forbidden.", response = MessageResponse.class),
            @ApiResponse(code = 404, message = "URL Not Found.", response = MessageResponse.class),
            @ApiResponse(code = 406, message = "Unauthorize Token", response = MessageResponse.class),
            @ApiResponse(code = 409, message = "Invalid Request.", response = MessageResponse.class),
            @ApiResponse(code = 500, message = "Server Error.", response = MessageResponse.class),
    })
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<T> partialUpdate(@RequestBody Map<String, Object> body, @PathVariable("id") ID id);


    @ApiOperation(value = "Remove Data", notes = "Remove data by Unique ID.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 204, message = "Not Found Data.", response = MessageResponse.class),
            @ApiResponse(code = 401, message = "Permission Denied.", response = MessageResponse.class),
            @ApiResponse(code = 403, message = "Access Forbidden.", response = MessageResponse.class),
            @ApiResponse(code = 404, message = "URL Not Found.", response = MessageResponse.class),
            @ApiResponse(code = 406, message = "Unauthorize Token", response = MessageResponse.class),
            @ApiResponse(code = 409, message = "Invalid Request.", response = MessageResponse.class),
            @ApiResponse(code = 500, message = "Server Error.", response = MessageResponse.class),
    })
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<MessageResponse> remove(@PathVariable("id") ID id);

    @ApiOperation(value = "Remove Multi Data", notes = "Modified data by JSON Array.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Invalid Parameter.", response = MessageResponse.class),
            @ApiResponse(code = 401, message = "Permission Denied.", response = MessageResponse.class),
            @ApiResponse(code = 403, message = "Access Forbidden.", response = MessageResponse.class),
            @ApiResponse(code = 404, message = "URL Not Found.", response = MessageResponse.class),
            @ApiResponse(code = 406, message = "Unauthorize Token", response = MessageResponse.class),
            @ApiResponse(code = 409, message = "Invalid Request.", response = MessageResponse.class),
            @ApiResponse(code = 500, message = "Server Error.", response = MessageResponse.class),
    })
    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<MessageResponse> multiRemove(@Valid @RequestBody List<ID> ids);


    @ApiOperation(value = "Import Data", notes = "Create, Modified data by data list.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Invalid Data.", response = MessageResponse.class),
            @ApiResponse(code = 401, message = "Permission Denied.", response = MessageResponse.class),
            @ApiResponse(code = 403, message = "Access Forbidden.", response = MessageResponse.class),
            @ApiResponse(code = 404, message = "URL Not Found.", response = MessageResponse.class),
            @ApiResponse(code = 406, message = "Unauthorize Token", response = MessageResponse.class),
            @ApiResponse(code = 409, message = "Invalid Request.", response = MessageResponse.class),
            @ApiResponse(code = 500, message = "Server Error.", response = MessageResponse.class),
    })
    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ListResponse<T>> importData(@Valid @RequestBody List<T> body);
}