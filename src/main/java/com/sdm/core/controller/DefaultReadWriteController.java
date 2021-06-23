package com.sdm.core.controller;

import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;

import javax.persistence.Id;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public abstract class DefaultReadWriteController<T extends DefaultEntity, ID extends Serializable>
        extends DefaultReadController<T, ID>
        implements ReadController<T, ID>, WriteController<T, ID> {

    @Override
    public ResponseEntity<T> create(@Valid T body) {
        T entity = getRepository().save(body);
        return new ResponseEntity<>(entity, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<T> update(@Valid T body, ID id) {
        this.checkData(id);
        if (!id.equals(body.getId())) {
            throw new GeneralException(HttpStatus.CONFLICT,
                    "Path ID and body ID aren't match.");
        }

        T entity = getRepository().save(body);
        return ResponseEntity.ok(entity);
    }

    @Override
    public ResponseEntity<T> partialUpdate(Map<String, Object> body, ID id) {
        T existEntity = this.checkData(id);

        body.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(getEntityClass(), key);
            if (field != null && !field.isAnnotationPresent(Id.class)) {
                ReflectionUtils.makeAccessible(field);
                ReflectionUtils.setField(field, existEntity, value);
            }
        });

        T entity = getRepository().save(existEntity);
        return ResponseEntity.ok(entity);
    }

    @Override
    public ResponseEntity<MessageResponse> remove(ID id) {
        T existEntity = this.checkData(id);
        getRepository().softDelete(existEntity);
        MessageResponse message = new MessageResponse("DELETED",
                "Deleted data on your request by : " + id.toString());
        return ResponseEntity.ok(message);
    }

    @Override
    @Transactional
    public ResponseEntity<MessageResponse> multiRemove(@Valid List<ID> ids) {
        ids.forEach(id -> getRepository().softDeleteById(id));
        MessageResponse message = new MessageResponse(HttpStatus.OK, "DELETED",
                "Deleted " + ids.size() + " data.", null);
        return ResponseEntity.ok(message);
    }

    @Override
    @Transactional
    public ResponseEntity<ListResponse<T>> importData(@Valid List<T> body) {
        List<T> data = getRepository().saveAll(body);
        return new ResponseEntity<>(new ListResponse<>(data), HttpStatus.OK);
    }
}
