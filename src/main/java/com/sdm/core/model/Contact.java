package com.sdm.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Contact implements Serializable {
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;
    @NotBlank
    @Size(max = 100)
    @Column(columnDefinition = "varchar(100)", nullable = false)
    private String label;
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String value;
    @Column(nullable = false, columnDefinition = "integer default 0")
    private int priority;
    @Column
    private Boolean verified;

    public enum Type {
        PHONE,
        EMAIL,
        SMS,
        URL,
        ADDRESS,
        LAT_LON,
        MESSAGING_ID,
        SOCIAL_ID,
    }
}
