package com.sdm.inventory.model;

import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Searchable;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

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
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "relatedUom")
    @NotFound(action = NotFoundAction.IGNORE)
    private UnitOfMeasurement relatedUom;

    @Column
    private Double relatedValue;

    @Override
    public Integer getId() {
        return this.id;
    }
}
