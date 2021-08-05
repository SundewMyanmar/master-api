package com.sdm.inventory.model;

import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Searchable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Audited
@Entity(name = "inventory.CategoryEntity")
@Table(name = "tbl_inventory_categories")
@Where(clause = "deleted_at IS NULL")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category extends DefaultEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Searchable
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @Searchable
    @NotBlank
    @Size(max = 500)
    @Column
    private String description;

    @Searchable
    @Size(max = 100)
    @Column(length = 100)
    private String icon;

    @Column
    private Integer parentId;

    @Override
    public Integer getId() {
        return this.id;
    }
}
