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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Audited
@Entity(name = "inventory.ProductReviewEntity")
@Table(name = "tbl_inventory_product_reviews")
@Where(clause = "deleted_at IS NULL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductReview extends DefaultEntity implements Serializable {
    @Id
    @Column(nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "productId", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Product product;

    @Searchable
    @NotBlank
    @Column(nullable = false)
    private int rating;

    @Searchable
    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500, columnDefinition = "varchar(500)")
    private String review;

    @Searchable
    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500, columnDefinition = "varchar(500)")
    private String reviewerId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, columnDefinition = "timestamp default current_timestamp")
    private Date reviewAt;

    @Column(columnDefinition = "char(36)")
    private String replyTo;
}
