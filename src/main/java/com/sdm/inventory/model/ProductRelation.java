package com.sdm.inventory.model;

import com.sdm.core.model.annotation.Searchable;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "inventory.ProductRelationEntity")
@Table(name = "tbl_inventory_product_relations")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRelation implements Serializable {
    @Id
    @Column(nullable = false, unique = true, columnDefinition = "char(36)")
    private String id;
    @Column(nullable = false)
    private int productId;
    @Searchable
    @Enumerated(EnumType.STRING)
    @Column
    private Type type;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "relatedProductId", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Product relatedProduct;
    @Searchable
    @Size(max = 500)
    @Column(columnDefinition = "varchar(500)", length = 500)
    private String remark;

    public enum Type {
        RELATED,
        VARIATION,
        UP_SELL,
        CROSS_SELL,
    }
}
