package com.sdm.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
@Where(clause = "deletedAt IS NULL")
public abstract class DefaultEntity implements Serializable {
    public abstract <T extends Serializable> T getId();

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column
    @Getter
    @Setter
    private Date deletedAt;
}