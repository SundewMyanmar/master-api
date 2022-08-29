package com.sdm.inventory.model;

import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Searchable;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Audited
@Entity(name = "inventory.ProductSetEntity")
@Table(name = "tbl_inventory_product_sets")
@Where(clause = "deleted_at IS NULL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductSet extends DefaultEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int productId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "includeProductId", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Product includeProduct;

    @Searchable
    @Column(nullable = false)
    private int qty;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean gift;

    @Override
    public Integer getId() {
        return this.id;
    }
}
