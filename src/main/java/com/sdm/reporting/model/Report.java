package com.sdm.reporting.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sdm.admin.model.Role;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Searchable;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Audited
@Entity(name = "ReportEntity")
@Table(name = "tbl_reports")
@Where(clause = "deleted_at IS NULL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Report extends DefaultEntity {
    @Id
    @Column(columnDefinition = "char(36)", length = 36, unique = true, nullable = false)
    private String id;

    @Searchable
    @Column(columnDefinition = "varchar(255)", nullable = false)
    private String name;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private boolean isPublic;

    @JsonIgnore
    private boolean hasDesign;

    @NotAudited
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinTable(name = "tbl_report_permission",
            joinColumns = {@JoinColumn(name = "reportId")},
            inverseJoinColumns = {@JoinColumn(name = "roleId")})
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;

    @Override
    public String getId(){
        return this.id;
    }
}
