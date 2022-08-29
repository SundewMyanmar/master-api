package com.sdm.inventory.model;

import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Searchable;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.envers.Audited;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Audited
@Entity(name = "inventory.PriceEntity")
@Table(name = "tbl_inventory_prices")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Price extends DefaultEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "productId", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Product product;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;
    @Column(nullable = false)
    private Double pricePerUnit;
    @Column
    private Long minQty;
    @Column
    private Float changedPercent;
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean includedDelivery;
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean includedTax;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, columnDefinition = "timestamp default current_timestamp")
    private Date startAt;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date endAt;
    @Searchable
    @Size(max = 500)
    @Column(columnDefinition = "varchar(500)", length = 500)
    private String remark;

    @Override
    public Long getId() {
        return this.id;
    }

    public enum Type {
        RETAIL,
        WHOLE,
        DISTRIBUTOR,
        DEALER,
        CONSIGNMENT,
    }
}
