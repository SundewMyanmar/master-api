package com.sdm.core.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.NotAudited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonPropertyOrder(value = {"id", "created_at", "modified_at"}, alphabetic = true)
@JsonIgnoreProperties(value = {"created_at", "modified_at"}, allowGetters = true)
public abstract class DefaultEntity implements Serializable {
    @JsonIgnore
    @Getter
    @Setter
    @NotAudited
    @CreatedBy
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "created_user_id", updatable = false)),
            @AttributeOverride(name = "token", column = @Column(name = "created_token", length = 36, columnDefinition = "char(36)", updatable = false))
    })
    private Auditor createdBy;
    @JsonIgnore
    @Getter
    @Setter
    @NotAudited
    @LastModifiedBy
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "modified_user_id")),
            @AttributeOverride(name = "token", column = @Column(name = "modified_token", length = 36, columnDefinition = "char(36)"))
    })
    private Auditor modifiedBy;
    @Getter
    @Setter
    @NotAudited
    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdAt;
    @Getter
    @Setter
    @NotAudited
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    private Date modifiedAt;
    @Getter
    @Setter
    @NotAudited
    @Version
    private int version;
    @Getter
    @Setter
    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date deletedAt;

    public abstract <T extends Serializable> T getId();

    @JsonGetter("createdBy")
    public int getCreatedUserId() {
        if (this.createdBy != null) {
            return this.createdBy.getId();
        }
        return 0;
    }

    @JsonGetter("modifiedBy")
    public int getModifiedUserId() {
        if (this.modifiedBy != null) {
            return this.modifiedBy.getId();
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultEntity)) return false;

        DefaultEntity that = (DefaultEntity) o;

        if (getVersion() != that.getVersion()) return false;
        if (getCreatedBy() != null ? !getCreatedBy().equals(that.getCreatedBy()) : that.getCreatedBy() != null)
            return false;
        if (getModifiedBy() != null ? !getModifiedBy().equals(that.getModifiedBy()) : that.getModifiedBy() != null)
            return false;
        if (getCreatedAt() != null ? !getCreatedAt().equals(that.getCreatedAt()) : that.getCreatedAt() != null)
            return false;
        if (getModifiedAt() != null ? !getModifiedAt().equals(that.getModifiedAt()) : that.getModifiedAt() != null)
            return false;
        return getDeletedAt() != null ? getDeletedAt().equals(that.getDeletedAt()) : that.getDeletedAt() == null;
    }

    @Override
    public int hashCode() {
        int result = getCreatedBy() != null ? getCreatedBy().hashCode() : 0;
        result = 31 * result + (getModifiedBy() != null ? getModifiedBy().hashCode() : 0);
        result = 31 * result + (getCreatedAt() != null ? getCreatedAt().hashCode() : 0);
        result = 31 * result + (getModifiedAt() != null ? getModifiedAt().hashCode() : 0);
        result = 31 * result + getVersion();
        result = 31 * result + (getDeletedAt() != null ? getDeletedAt().hashCode() : 0);
        return result;
    }
}