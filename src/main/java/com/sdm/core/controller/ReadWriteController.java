package com.sdm.core.controller;

import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.response.ListModel;
import com.sdm.core.model.response.MessageModel;
import com.sdm.core.util.CsvManager;
import com.sdm.core.util.Globalizer;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class ReadWriteController<T extends DefaultEntity, ID extends Serializable> extends ReadController<T, ID> {
    @PostMapping("/")
    public ResponseEntity create(@Valid @RequestBody T request) {
        T entity = getRepository().save(request);
        return new ResponseEntity(entity, HttpStatus.CREATED);
    }

    @PostMapping("/multi")
    ResponseEntity multiCreate(@Valid @RequestBody List<T> request) {
        List<T> data = getRepository().saveAll(request);
        return new ResponseEntity(new ListModel<T>(data), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@Valid @RequestBody T request, @PathVariable("id") ID id) {
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

    @PostMapping("/export")
    @Transactional
    ResponseEntity exportCsv()throws IOException,IllegalAccessException{
        CsvManager<T> csvManager=new CsvManager<>(this.getEntityClass());
        List<T> datas=getRepository().findAll();
        CacheControl cacheControl = CacheControl.maxAge(7, TimeUnit.DAYS);
        String attachment = "attachment; filename=\"" +this.getEntityClass().getName()+
                Globalizer.getDateString("yyyy-MM-dd", new Date()) + ".csv\"";
        Resource outputResource = csvManager.parseEntityToCsv(datas);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, attachment)
                .cacheControl(cacheControl)
                .body(outputResource);
    }
}
