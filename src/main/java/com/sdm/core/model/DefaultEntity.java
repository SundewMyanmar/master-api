package com.sdm.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sdm.core.db.DataLogging;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
@EntityListeners({DataLogging.class, AuditingEntityListener.class})
@JsonPropertyOrder(value = {"id", "created_at", "modified_at"}, alphabetic = true)
@JsonIgnoreProperties(value = {"created_at", "modified_at"}, allowGetters = true)
@EqualsAndHashCode
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