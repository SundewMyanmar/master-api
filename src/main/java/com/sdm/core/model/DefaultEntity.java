package com.sdm.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
@Where(clause = "deletedAt IS NULL")
@EntityListeners({AuditingEntityListener.class})
@JsonPropertyOrder(value = {"id", "created_at", "modified_at"}, alphabetic = true)
@JsonIgnoreProperties(value = {"created_at", "modified_at"}, allowGetters = true)
public abstract class DefaultEntity implements Serializable {
    public abstract <T extends Serializable> T getId();

    @JsonIgnore
    @CreatedBy
    @Column(length = 36, columnDefinition = "CHAR(36)", updatable = false)
    @Getter
    @Setter
    private String createdBy;

    @JsonIgnore
    @LastModifiedBy
    @Column(length = 36, columnDefinition = "CHAR(36)", updatable = false)
    @Getter
    @Setter
    private String modifiedBy;

    @Getter
    @Setter
    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdAt;

    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    @Getter
    @Setter
    private Date modifiedAt;


    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column
    @Getter
    @Setter
    private Date deletedAt;
}