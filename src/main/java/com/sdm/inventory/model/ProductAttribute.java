package com.sdm.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity(name = "inventory.ProductAttributeEntity")
@Table(name = "tbl_inventory_product_attributes")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductAttribute implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private int productId;

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
