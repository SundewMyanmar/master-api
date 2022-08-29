package com.sdm.inventory.model;

import com.sdm.storage.model.File;

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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
