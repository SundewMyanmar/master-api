package com.sdm.core.model;

import com.sdm.core.db.HibernateAuditListener;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "AuditEntity")
@Table(name = "tbl_audit_info")
@RevisionEntity(HibernateAuditListener.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SundewAuditEntity extends DefaultRevisionEntity {

    @Column
    private int userId;

    @Column
    private String token;
}