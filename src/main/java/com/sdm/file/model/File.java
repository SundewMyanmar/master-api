/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.file.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Filterable;
import com.sdm.core.util.FileManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
@AllArgsConstructor
@NoArgsConstructor
public class File extends DefaultEntity implements Serializable {

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
    @Column(columnDefinition = "char(36)", length = 36, unique = true, nullable = false)
    private String id;

    @Filterable
    @NotBlank
    @Column(columnDefinition = "varchar(255)", length = 255, nullable = false)
    private String name;

    @NotBlank
    @Column(columnDefinition = "varchar(10)", length = 10, nullable = false)
    private String extension;

    @Column(columnDefinition = "varchar(50)", length = 50, nullable = false)
    private String type;

    @JsonIgnore
    @Column(columnDefinition = "INT", nullable = false)
    private long fileSize;

    @JsonIgnore
    @Column(columnDefinition = "varchar(1000)", length = 1000)
    private String storagePath;

    @Column(columnDefinition = "varchar(1000)", length = 1000)
    private String externalUrl;

    @Column(nullable = false)
    private boolean publicAccess;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public File(String id, String name, String extension, String type, long fileSize,
                String storagePath, String externalUrl) {
        if (StringUtils.isEmpty(externalUrl)) {
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

    @JsonGetter("urls")
    public Map<String, String> getUrls() {
        Map<String, String> urls = new HashMap<>();
        if (this.publicAccess) {
            urls.put("public", ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/public/files/")
                    .path(this.id + "/")
                    .path(this.name + "." + this.extension)
                    .toUriString());
        } else {
            urls.put("private", ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/files/download/")
                    .path(this.id + "/")
                    .path(this.name + "." + this.extension)
                    .toUriString());
        }

        if (!StringUtils.isEmpty(this.externalUrl)) {
            urls.put("external", this.externalUrl);
        }

        return urls;
    }

    @JsonGetter("size")
    public String getSize() {
        return FileManager.byteSize(fileSize);
    }

    @Override
    public String getId() {
        return id;
    }

}
