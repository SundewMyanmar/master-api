package com.sdm.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sdm.core.db.HibernateAuditListener;

import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity(name = "AuditEntity")
@Table(name = "tbl_audit_info")
@RevisionEntity(HibernateAuditListener.class)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SundewAuditEntity {

    @Id
    @GeneratedValue
    @RevisionNumber
    private Long id;

    @RevisionTimestamp
    private long timestamp;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "user_id", updatable = false)),
            @AttributeOverride(name = "token", column = @Column(name = "auth_token", length = 36, columnDefinition = "char(36)", updatable = false))
    })
    private Auditor auditor;
}