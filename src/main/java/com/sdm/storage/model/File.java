/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.storage.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Searchable;
import com.sdm.core.util.Globalizer;
import com.sdm.storage.service.FileService;

import lombok.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Htoonlin
 */
@Audited
@Entity(name = "FileEntity")
@Table(name = "tbl_files")
@Where(clause = "deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class File extends DefaultEntity implements Serializable {


    /**
     *
     */
    private static final long serialVersionUID = 2692423129475255385L;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "folderId")
    @NotFound(action = NotFoundAction.IGNORE)
    private Folder folder;
    @Id
    @Column(columnDefinition = "char(36)", length = 36, unique = true, nullable = false)
    private String id;
    @Searchable
    @NotBlank
    @Column(columnDefinition = "varchar(255)", nullable = false)
    private String name;
    @Searchable
    @Column(columnDefinition = "varchar(255)")
    private String guild;
    @NotBlank
    @Column(columnDefinition = "varchar(10)", length = 10, nullable = false)
    private String extension;
    @Column(columnDefinition = "varchar(255)", nullable = false)
    private String type;
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @CollectionTable(name = "tbl_file_attributes",
            joinColumns = @JoinColumn(name = "fileId", nullable = false))
    private Map<String, String> attributes;
    @JsonIgnore
    @Column(columnDefinition = "INT", nullable = false)
    private long fileSize;
    @JsonIgnore
    @Column(columnDefinition = "varchar(1000)", length = 1000)
    private String storagePath;
    @Column(nullable = false)
    private boolean publicAccess;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public File(String id, String name, String extension, String type, long fileSize,
                String storagePath, String externalUrl) {
        this.status = Status.STORAGE;
        this.id = id;
        this.name = name;
        this.extension = extension;
        this.type = type;
        this.fileSize = fileSize;
        this.storagePath = storagePath;
    }

    @JsonGetter("urls")
    public Map<String, String> getUrls() {
        Map<String, String> urls = new HashMap<>();
        if (this.publicAccess) {
            urls.put("public",
                    Globalizer.getCurrentContextBuilder(false)
                            .path("/public/files/")
                            .path(this.id + "/")
                            .path(this.name + "." + this.extension)
                            .toUriString());
        } else {
            urls.put("private",
                    Globalizer.getCurrentContextBuilder(false)
                            .path("/files/download/")
                            .path(this.id + "/")
                            .path(this.name + "." + this.extension)
                            .toUriString());
        }

        return urls;
    }

    @JsonGetter("size")
    public String getSize() {
        return FileService.byteSize(fileSize);
    }

    @Override
    public String getId() {
        return id;
    }

    public void addAttribute(String key, String value) {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
        }
        this.attributes.put(key, value);
    }

    public enum Status {
        STORAGE,
        TRASH,
        HIDDEN
    }

    public enum ImageSize {
        original(0),
        large(1024),
        medium(512),
        small(256),
        thumb(64);

        private final int maxSize;

        ImageSize(int size) {
            this.maxSize = size;
        }

        public int getMaxSize() {
            return maxSize;
        }
    }

}