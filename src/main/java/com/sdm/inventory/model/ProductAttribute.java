package com.sdm.inventory.model;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "inventory.ProductAttributeEntity")
@Table(name = "tbl_inventory_product_attributes")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductAttribute implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = true)
    private Integer productId;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "attributeId")
    @NotFound(action = NotFoundAction.IGNORE)
    private Attribute attribute;

    @NotBlank
    @Column(columnDefinition = "text")
    private String value;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uomId")
    @NotFound(action = NotFoundAction.IGNORE)
    private UnitOfMeasurement uom;
}
