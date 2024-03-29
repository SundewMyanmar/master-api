package com.sdm.storage.model;

import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Searchable;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Audited
@Entity(name = "FolderEntity")
@Table(name = "tbl_file_folders")
@Where(clause = "deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Folder extends DefaultEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Searchable
    @NotBlank
    @Size(min = 1, max = 250)
    @Column(nullable = false, length = 250)
    private String name;

    @Searchable
    @Size(min = 1, max = 250)
    @Column(nullable = true, length = 250)
    private String guild;

    @Searchable
    @Size(max = 20)
    @Column(length = 20)
    private String color;

    @Searchable
    @Size(max = 100)
    @Column(length = 100)
    private String icon;

    @Column(nullable = false)
    private int priority;

    @Column
    private Integer parentId;

    @NotAudited
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "parentId")
    @OneToMany(fetch = FetchType.EAGER)
    @OrderBy("priority")
    private Set<Folder> items;

    @Override
    public Integer getId() {
        return this.id;
    }
}
