package com.sdm.master.entity;

import com.sdm.core.model.DefaultEntity;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * @Author ayeyin
 * @Since 2017-11-17 14:33:42
 */
@Entity(name = "MenuEntity")
@Table(name = "tbl_menus")
public class MenuEntity extends DefaultEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -6187216010093509650L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, columnDefinition = "varchar(50)")
    private String name;

    @Column
    private String description;

    @Column(nullable = false, columnDefinition = "varchar(500)")
    private String state;

    @Column(nullable = false, columnDefinition = "varchar(50)")
    private String icon;

    /**
     * Supported Types : module, toggle, link
     */
    @Column(nullable = false, columnDefinition = "varchar(10)")
    private String type;

    @Column(nullable = false)
    private int priority;

    @Column(nullable = false)
    private boolean divider;

    @Column(nullable = true,name = "parent_id")
    private Integer parentId;

    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "parent_id")
    @OneToMany(fetch = FetchType.EAGER)
    private Set<MenuEntity> children;

    @NotFound(action = NotFoundAction.IGNORE)
    @JoinTable(name = "tbl_menu_permissions",
        joinColumns = {@JoinColumn(name = "menu_id")},
        inverseJoinColumns = {@JoinColumn(name = "role_id")})
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<RoleEntity> roles;

    public MenuEntity() {
    }

    public MenuEntity(int id, String name, String description, String state, String icon, String type, int priority, boolean separator,Integer parentId) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
        this.state = state;
        this.icon = icon;
        this.type = type;
        this.priority = priority;
        this.divider = separator;
        this.parentId=parentId;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean getDivider() {
        return divider;
    }

    public void setDivider(boolean divider) {
        this.divider = divider;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Set<MenuEntity> getChildren() {
        return children;
    }

    public void setChildren(Set<MenuEntity> children) {
        this.children = children;
    }

    public Set<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MenuEntity other = (MenuEntity) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }
}
