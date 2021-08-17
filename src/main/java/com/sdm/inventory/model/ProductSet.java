package com.sdm.inventory.model;

import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Searchable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;

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
