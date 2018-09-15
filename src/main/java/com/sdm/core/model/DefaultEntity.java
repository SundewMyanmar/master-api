/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sdm.core.component.JpaAuditListener;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author Htoonlin
 */
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class, JpaAuditListener.class})
@JsonIgnoreProperties(value = {"created_at", "modified_at"}, allowGetters = true)
public class DefaultEntity implements Serializable {

    private static final long serialVersionUID = -1235673932545866165L;

    @JsonIgnore
    @CreatedBy
    @Column(length = 36, columnDefinition = "CHAR(36)")
    private String createdBy;

    @JsonIgnore
    @LastModifiedBy
    @Column(length = 36, columnDefinition = "CHAR(36)")
    private String lastModifiedBy;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdAt;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    private Date lastModifiedAt;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(Date lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    @JsonIgnore
    public HashMap<String, Object> getQueries() {

        String entityName = this.getClass().getName();
        Entity entityAnno = this.getClass().getAnnotation(Entity.class);
        if (entityAnno != null) {
            entityName = entityAnno.name();
        }

        HashMap<String, Object> queries = new HashMap<>();
        NamedQueries namedQueries = this.getClass().getAnnotation(NamedQueries.class);
        if (namedQueries != null) {
            for (NamedQuery query : namedQueries.value()) {
                String name = query.name().substring(entityName.length() + 1);
                queries.put(name, query.query());
            }
        }

        for (NamedQuery query : this.getClass().getAnnotationsByType(NamedQuery.class)) {
            queries.put(query.name(), query.query());
        }

        return queries;
    }

    @JsonIgnore
    public List<?> getStructure() {
        /*List<UIProperty> properties = new ArrayList<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            // Check has annotations
            if (field.getAnnotations().length <= 0) {
                continue;
            }

            // Check JsonIgnore || Transient
            if (field.getAnnotation(JsonIgnore.class) != null || field.getAnnotation(Transient.class) != null) {
                continue;
            }

            UIProperty property = new UIProperty();
            // General info
            property.setName(field.getName());
            property.setType(field.getType().getSimpleName());

            if (field.getAnnotation(Id.class) != null) {
                property.setPrimary(true);
            }

            // UI Info
            UIStructure structure = field.getAnnotation(UIStructure.class);
            if (structure != null) {
                property.setInputType(structure.inputType());
                property.setLabel(structure.label());
                property.setHideInGrid(structure.hideInGrid());
                property.setReadOnly(structure.readOnly());
                property.setOrderIndex(structure.order());
            }

            // Db Info
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                if (!column.nullable()) {
                    property.setRequired(!column.nullable());
                }
                property.setLength(column.length());
            }

            // Validations Info
            properties.add(property);
        }

        Collections.sort(properties, new Comparator<UIProperty>() {
            @Override
            public int compare(UIProperty t1, UIProperty t2) {
                return Integer.compare(t1.getOrderIndex(), t2.getOrderIndex());
            }
        });*/

        return null;
    }


}
