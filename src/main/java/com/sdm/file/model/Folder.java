package com.sdm.file.model;

import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Searchable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Set;

@Audited
@Entity(name = "FolderEntity")
@Table(name = "tbl_file_folders")
@Where(clause = "deleted_at IS NULL")
@Data
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
