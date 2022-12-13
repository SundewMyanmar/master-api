package com.sdm.inventory.model;

import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Searchable;

import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Audited
@Entity(name = "inventory.AttributeEntity")
@Table(name = "tbl_inventory_attributes")
@Where(clause = "deleted_at IS NULL")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attribute extends DefaultEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Searchable
    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String name;
    @Searchable
    @NotBlank
    @Size(max = 500)
    @Column(length = 500)
    private String description;
    @Searchable
    @Size(max = 50)
    @Column(length = 50)
    private String guild;
    @Searchable
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @NotAudited
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tbl_inventory_attribute_allow_values", joinColumns = @JoinColumn(name = "attributeId"))
    @Column
    private Set<String> allowedValues = new HashSet<>();

    @Column(columnDefinition = "boolean default false")
    private boolean hasUom;

    @Column(columnDefinition = "boolean default false")
    private boolean searchable;

    @Override
    public Integer getId() {
        return this.id;
    }

    public enum Type {
        TEXT,
        LONG_TEXT,
        HTML,
        COLOR,
        INTEGER,
        FLOAT,
        DATE,
        DATETIME,
        CHOICE,
        LIST,
        YES_NO,
    }
}
