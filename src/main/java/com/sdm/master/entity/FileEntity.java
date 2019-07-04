/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.master.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Filterable;
import com.sdm.core.util.FileManager;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Htoonlin
 */
@Entity(name = "FileEntity")
@Table(name = "tbl_files")
public class FileEntity extends DefaultEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2692423129475255385L;

    public enum Status {
        STORAGE,
        EXTERNAL,
        TRASH
    }

    @Id
    @Column(name = "id", columnDefinition = "char(36)", length = 36, unique = true, nullable = false)
    private String id;

    @Filterable
    @NotBlank
    @Column(name = "name", columnDefinition = "varchar(255)", length = 255, nullable = false)
    private String name;

    @NotBlank
    @Column(name = "extension", columnDefinition = "varchar(10)", length = 10, nullable = false)
    private String extension;

    @Column(name = "type", columnDefinition = "varchar(50)", length = 50, nullable = false)
    private String type;

    @JsonIgnore
    @Column(name = "size", columnDefinition = "INT UNSIGNED", nullable = false)
    private long fileSize;

    @JsonIgnore
    @Column(name = "storage_path", columnDefinition = "varchar(1000)", length = 1000, nullable = true)
    private String storagePath;

    @Column(name = "external_url", columnDefinition = "varchar(1000)", length = 1000, nullable = true)
    private String externalUrl;

    @Column(name = "is_public", length = 25)
    private boolean publicAccess;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    public FileEntity() {
        this.status = Status.STORAGE;
    }

    public FileEntity(String id, String name, String extension, String type, long fileSize,
                      String storagePath, String externalUrl) {
        if (externalUrl == null || externalUrl.length() <= 0) {
            this.status = Status.STORAGE;
        } else {
            this.status = Status.EXTERNAL;
        }
        this.id = id;
        this.name = name;
        this.extension = extension;
        this.type = type;
        this.fileSize = fileSize;
        this.storagePath = storagePath;
    }

    @JsonGetter("download_url")
    public String getDownloadUrl() {
        String downloadURL = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/files/download/")
                .path(this.id + "/")
                .path(this.name + "." + this.extension)
                .toUriString();
        return downloadURL;
    }

    @JsonGetter("public_url")
    public String getPublicUrl() {
        if (!this.publicAccess) {
            return null;
        }

        if (this.externalUrl != null && this.externalUrl.length() > 0) {
            return this.externalUrl;
        }

        String downloadURL = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/public/files/")
                .path(this.id + "/")
                .path(this.name + "." + this.extension)
                .toUriString();
        return downloadURL;
    }

    @JsonGetter("size")
    public String getSize() {
        return FileManager.byteSize(fileSize);
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long size) {
        this.fileSize = size;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public boolean isPublicAccess() {
        return publicAccess;
    }

    public void setPublicAccess(boolean publicAccess) {
        this.publicAccess = publicAccess;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileEntity other = (FileEntity) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.id);
        return hash;
    }

}
