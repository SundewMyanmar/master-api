/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.admin.model;

import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Filterable;
import com.sdm.core.model.annotation.Grid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Htoonlin
 */
@Audited
@Entity(name = "admin.RoleEntity")
@Table(name = "tbl_admin_roles")
@Where(clause = "deleted_at IS NULL")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role extends DefaultEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 739168064520778219L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Filterable
    @NotBlank
    @Size(min = 1, max = 255)
    @Grid(value = "Role Name", minWidth = 150, sortable = true)
    @Column(unique = true, length = 255, nullable = false)
    private String name;

    @Filterable
    @Size(max = 500)
    @Grid(value = "Full Description", minWidth = 350, sortable = true)
    @Column(columnDefinition = "varchar(500)", length = 500, nullable = false)
    private String description;

    @Override
    public Integer getId() {
        return id;
    }
}
