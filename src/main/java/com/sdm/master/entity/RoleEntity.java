/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.master.entity;

import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Filterable;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Htoonlin
 */
@Entity(name = "RoleEntity")
@Table(name = "tbl_roles")
public class RoleEntity extends DefaultEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 739168064520778219L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Filterable
    @Column(unique = true, nullable = false)
    private String name;

    @Filterable
    @Column(columnDefinition = "varchar(500)", length = 500, nullable = false)
    private String description;

    public RoleEntity() {
    }

    public RoleEntity(String name) {
        this.name = name;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NotBlank
    @Size(min = 1, max = 255)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Size(min = 0, max = 500)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RoleEntity other = (RoleEntity) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

}
