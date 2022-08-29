package com.sdm.auth.model.request;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivateRequest implements Serializable {
    @NotBlank
    private String user;

    @NotBlank
    private String token;
}
