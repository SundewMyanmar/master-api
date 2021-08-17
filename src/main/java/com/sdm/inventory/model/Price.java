package com.sdm.inventory.model;

import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Searchable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

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
