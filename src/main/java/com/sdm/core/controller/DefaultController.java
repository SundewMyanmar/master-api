package com.sdm.core.controller;

import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.AuthInfo;
import com.sdm.core.model.annotation.FileClassification;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.LocaleManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DefaultController {

    @Autowired
    protected LocaleManager localeManager;

    protected FileClassification getFileClassification(Class<?> entityClass, String fieldName) {
        try {
            Field field = entityClass.getDeclaredField(fieldName);
            if (!field.isAnnotationPresent(FileClassification.class)) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, "There is no file classification.");
            }
            return field.getAnnotation(FileClassification.class);
        } catch (NoSuchFieldException fieldException) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, "Invalid field name.");
        }
    }

    protected AuthInfo getCurrentUser() {
        return (AuthInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    protected Pageable buildPagination(int pageId, int pageSize, String sortString) {
        sortString = sortString.replaceAll("\\s+", "");
        List<Sort.Order> sorting = new ArrayList<>();
        if (!Globalizer.isNullOrEmpty(sortString)) {
            String[] sorts = sortString.split(",");
            for (String sort : sorts) {
                String[] sortParams = sort.strip().split(":", 2);
                if (sortParams.length >= 2 && sortParams[1].equalsIgnoreCase("desc")) {
                    sorting.add(Sort.Order.desc(sortParams[0]));
                } else {
                    sorting.add(Sort.Order.asc(sortParams[0]));
                }
            }
        }
        return PageRequest.of(pageId, pageSize, Sort.by(sorting));
    }
}
