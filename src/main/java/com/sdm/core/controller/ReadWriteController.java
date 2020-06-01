package com.sdm.core.controller;

import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.MessageResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface ReadWriteController<T, ID extends Serializable> extends ReadController<T, ID> {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<T> create(@Valid @RequestBody T body);

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<T> update(@Valid @RequestBody T body, @PathVariable("id") ID id);

    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<T> partialUpdate(@RequestBody Map<String, Object> body, @PathVariable("id") ID id);

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<MessageResponse> remove(@PathVariable("id") ID id);

    @DeleteMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<MessageResponse> multiRemove(@Valid @RequestBody List<ID> ids);

    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ListResponse<T>> importData(@Valid @RequestBody List<T> body);
}
