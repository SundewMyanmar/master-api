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
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Audited
@Entity(name = "inventory.ProductEntity")
@Table(name = "tbl_inventory_products")
@Where(clause = "deleted_at IS NULL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product extends DefaultEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Searchable
    @NotBlank
    @Size(max = 50)
    @Column(unique = true, length = 50)
    private String code;
    @Searchable
    @Size(max = 50)
    @Column(length = 50)
    private String barCode;
    @Searchable
    @Enumerated(EnumType.STRING)
    @Column
    private BarcodeType barcodeType;
    @Searchable
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;
    @Searchable
    @Size(max = 500)
    @Column(columnDefinition = "varchar(500)")
    private String shortDescription;
    @Column(columnDefinition = "text")
    private String description;
    @NotAudited
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tbl_inventory_product_tags", joinColumns = @JoinColumn(name = "productId"))
    @Column
    private Set<String> tags = new HashSet<>();
    @NotAudited
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "productId")
    @NotFound(action = NotFoundAction.IGNORE)
    private Set<ProductAttribute> attributes = new HashSet<>();
    @NotAudited
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "productId")
    @NotFound(action = NotFoundAction.IGNORE)
    private Set<ProductAttachment> attachments = new HashSet<>();
    @NotAudited
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "tbl_inventory_product_categories",
            joinColumns = {@JoinColumn(name = "productId")},
            inverseJoinColumns = {@JoinColumn(name = "categoryId")})
    @NotFound(action = NotFoundAction.IGNORE)
    private Set<Category> categories = new HashSet<>();
    @Searchable
    @Column
    private Long currentBal;
    @Searchable
    @Column
    private Integer minBal;
    @Searchable
    @Column
    private Long maxBal;
    @Searchable
    @Column
    private Integer reorderBal;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "balUom")
    @NotFound(action = NotFoundAction.IGNORE)
    private UnitOfMeasurement uom;
    @Searchable
    @Size(max = 500)
    @Column(columnDefinition = "varchar(500)", length = 500)
    private String remark;
    @Enumerated(EnumType.STRING)
    @Column
    private Status status;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, columnDefinition = "timestamp default current_timestamp")
    private Date activeAt;

    @Override
    public Integer getId() {
        return this.id;
    }

    /**
     * Reference from https://www.scandit.com/blog/types-barcodes-choosing-right-barcode/
     */
    public enum BarcodeType {
        UPC_A,
        UPC_E,
        EAN_13,
        EAN_8,
        CODE_39,
        CODE_128,
        ITF_14,
        PDF_417,
        AZTEC,
        DATA_MATRIX,
        QR,
    }

    public enum Status {
        AVAILABLE,
        UNAVAILABLE,
        OUT_OF_STOCK,
    }
}
