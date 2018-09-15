/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.master.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.util.FileManager;
import org.hibernate.annotations.Formula;

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
    public static final char STORAGE = 'S';
    public static final char EXTERNAL = 'E';
    public static final char TRASH = 'T';

    @JsonIgnore
    @Formula(value = "concat(name, extension, type)")
    private String search;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private long id;

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

    @Column(name = "status", columnDefinition = "char(1)", length = 1, nullable = false)
    private char status;

    public FileEntity() {
        this.status = STORAGE;
    }

    public FileEntity(long id, String name, String extension, String type, long fileSize,
                      String storagePath, String externalUrl) {
        if (externalUrl == null || externalUrl.length() <= 0) {
            this.status = STORAGE;
        } else {
            this.status = EXTERNAL;
        }
        this.id = id;
        this.name = name;
        this.extension = extension;
        this.type = type;
        this.fileSize = fileSize;
        this.storagePath = storagePath;
    }

    /*@JsonGetter("&file_links")
    public Map<String, LinkModel> getLinks() {
        UriBuilder baseUri = UriBuilder.fromResource(FileResource.class).path(Long.toString(id));
        Map<String, LinkModel> links = new HashMap<>();
        String selfLink = baseUri.build().toString();
        links.put("self", new LinkModel(selfLink));

        baseUri.path("{access}");

        String privateDownload = baseUri.build("download").toString();
        links.put("private", new LinkModel(privateDownload));

        String thumbnailDownload = baseUri.build("thumbnail").toString();
        links.put("thumbnail", new LinkModel(thumbnailDownload));

        if (this.isPublicAccess()) {
            String publicDownload = baseUri.build("public").toString();
            links.put("public", new LinkModel(publicDownload));

            String publicthumbnailDownload = baseUri.build("public/thumbnail").toString();
            links.put("publicThumbnail", new LinkModel(publicthumbnailDownload));
        }

        return links;
    }*/

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    @JsonGetter("size")
    public String getSize() {
        return FileManager.byteSize(fileSize);
    }

    @JsonSetter("size")
    public void setSize() {

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

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
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
