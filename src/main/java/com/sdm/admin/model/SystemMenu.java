package com.sdm.admin.model;

import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Filterable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Set;

@Audited
@Entity(name = "admin.SystemMenuEntity")
@Table(name = "tbl_admin_system_menus")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemMenu extends DefaultEntity implements Serializable {

    public enum Type {
        MODULE,
        TOGGLE,
        LINK
    }

    /**
     *
     */
    private static final long serialVersionUID = -6187216010093509650L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Filterable
    @NotBlank
    @Size(min = 1, max = 50)
    @Column(nullable = false, length = 50)
    private String name;

    @Filterable
    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @Filterable
    @Size(max = 500)
    @Column(length = 500)
    private String state;

    @Filterable
    @Size(max = 100)
    @Column(length = 100)
    private String icon;

    /**
     * Supported Types : module, toggle, link
     */
    @Filterable
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private int priority;

    @Column(nullable = false)
    private boolean divider;

    @Column
    private Integer parentId;

    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "parentId")
    @OneToMany(fetch = FetchType.EAGER)
    private Set<SystemMenu> children;

    @NotFound(action = NotFoundAction.IGNORE)
    @JoinTable(name = "tbl_admin_menu_permissions",
            joinColumns = {@JoinColumn(name = "menuId")},
            inverseJoinColumns = {@JoinColumn(name = "roleId")})
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;

    @Override
    public Integer getId() {
        return id;
    }
}
