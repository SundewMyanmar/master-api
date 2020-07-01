package com.sdm.core.model;

import com.sdm.core.db.HibernateAuditListener;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import javax.persistence.*;

@Entity(name = "AuditEntity")
@Table(name = "tbl_audit_info")
@RevisionEntity(HibernateAuditListener.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SundewAuditEntity {

    @Id
    @GeneratedValue
    @RevisionNumber
    private Long version;

    @RevisionTimestamp
    private long timestamp;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "user_id", updatable = false)),
            @AttributeOverride(name = "token", column = @Column(name = "auth_token", length = 36, columnDefinition = "char(36)", updatable = false))
    })
    private Auditor auditor;
}