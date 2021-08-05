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
@Entity(name = "inventory.UnitOfMeasurementEntity")
@Table(name = "tbl_inventory_uoms")
@Where(clause = "deleted_at IS NULL")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnitOfMeasurement extends DefaultEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Searchable
    @NotBlank
    @Size(max = 20)
    @Column(length = 20, nullable = false)
    private String code;

    @Searchable
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @Searchable
    @Size(max = 50)
    @Column(length = 50)
    private String guild;

    @Column
    private Integer relatedUom;

    @Column
    private Double relatedValue;

    @Override
    public Integer getId() {
        return this.id;
    }
}
