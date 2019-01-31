package com.sdm.core.controller;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.response.ListModel;
import com.sdm.core.model.response.MessageModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

public abstract class ReadWriteController<T extends DefaultEntity, ID extends Serializable> extends ReadController<T, ID> {
    @PostMapping("/")
    ResponseEntity create(@Valid @RequestBody T request) {
        T entity = getRepository().save(request);
        return new ResponseEntity(entity, HttpStatus.CREATED);
    }

    @PostMapping("/multi")
    ResponseEntity multiCreate(@Valid @RequestBody List<T> request) {
        List<T> data = getRepository().saveAll(request);
        return new ResponseEntity(new ListModel<T>(data), HttpStatus.CREATED);
    }

    @Transactional
    @PostMapping("/import")
    ResponseEntity importData(@RequestParam("uploadedFile") MultipartFile file) throws Exception {
        CsvSchema bootstrapSchema = CsvSchema.emptySchema().withHeader();
        MappingIterator<T> entities = getCsvMapper().readerWithSchemaFor(this.getEntityClass())
            .with(bootstrapSchema).readValues(file.getInputStream());

        int count = 0;
        while (entities.hasNextValue()) {
            T entity = entities.nextValue();
            getRepository().save(entity);
            count++;
        }

        MessageModel message = MessageModel.createMessage("Success",
            "Created " + count + " record(s).");

        return new ResponseEntity(message, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    ResponseEntity update(@Valid @RequestBody T request, @PathVariable("id") ID id) {
        this.checkData(id);
        if (!id.equals(request.getId())) {
            throw new GeneralException(HttpStatus.CONFLICT,
                "Path ID and body ID aren't match.");
        }

        T entity = getRepository().save(request);
        return ResponseEntity.ok(entity);
    }

    @PutMapping("/multi")
    ResponseEntity multiUpdate(@Valid @RequestBody List<T> request) {
        List<T> data = getRepository().saveAll(request);
        return ResponseEntity.ok(new ListModel<T>(data));
    }

    @DeleteMapping("/{id}")
    ResponseEntity remove(@PathVariable("id") ID id) {
        T existEntity = this.checkData(id);
        getRepository().delete(existEntity);
        MessageModel message = MessageModel.createMessage("Successfully deleted.",
            "Deleted data on your request by : " + id.toString());
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/multi")
    @Transactional
    ResponseEntity multiRemove(@RequestBody Set<ID> ids) {
        ids.forEach(id -> getRepository().deleteById(id));
        MessageModel message = MessageModel.createMessage("Successfully deleted.",
            "Deleted data on your request.");
        return ResponseEntity.ok(message);
    }

}
