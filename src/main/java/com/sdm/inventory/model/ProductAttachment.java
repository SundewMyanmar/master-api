package com.sdm.inventory.model;

import com.sdm.storage.model.File;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity(name = "inventory.ProductAttachmentEntity")
@Table(name = "tbl_inventory_product_attachments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductAttachment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = true)
    private Integer productId;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fileId")
    @NotFound(action = NotFoundAction.IGNORE)
    private File file;

    @Column(columnDefinition = "int default 0")
    private int priority;

    @Size(max = 50)
    @Column(length = 50)
    private String type;

    @Size(max = 50)
    @Column(length = 50)
    private String title;

    @Size(max = 255)
    @Column
    private String description;
}
