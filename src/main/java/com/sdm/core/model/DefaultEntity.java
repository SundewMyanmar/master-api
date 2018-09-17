/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sdm.core.component.JpaAuditListener;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Htoonlin
 */
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class, JpaAuditListener.class})
@JsonPropertyOrder(value = {"id", "created_at", "modified_at"}, alphabetic = true)
@JsonIgnoreProperties(value = {"created_at", "modified_at"}, allowGetters = true)
public abstract class DefaultEntity<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = -1235673932545866165L;

    public abstract T getId();

    @JsonIgnore
    @CreatedBy
    @Column(length = 36, columnDefinition = "VARCHAR(36)")
    private String createdBy;

    @JsonIgnore
    @LastModifiedBy
    @Column(length = 36, columnDefinition = "VARCHAR(36)")
    private String modifiedBy;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdAt;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    private Date modifiedAt;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

}
