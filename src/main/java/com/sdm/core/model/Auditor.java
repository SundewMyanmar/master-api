package com.sdm.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Auditor {
    private int id;

    @JsonIgnore
    private String token;
}
