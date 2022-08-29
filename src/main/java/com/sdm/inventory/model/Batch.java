package com.sdm.inventory.model;

import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Searchable;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Audited
@Entity(name = "inventory.BatchEntity")
@Table(name = "tbl_inventory_batches")
@Where(clause = "deleted_at IS NULL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Batch extends DefaultEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "productId", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Product product;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, columnDefinition = "timestamp default current_timestamp")
    private Date entryAt;

    @Searchable
    @Column(nullable = false)
    private long inBal;

    @Searchable
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long outBal;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date manufactureAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date expiredAt;

    @Searchable
    @Size(max = 500)
    @Column(columnDefinition = "varchar(500)", length = 500)
    private String remark;

    @Override
    public Long getId() {
        return this.id;
    }
}
