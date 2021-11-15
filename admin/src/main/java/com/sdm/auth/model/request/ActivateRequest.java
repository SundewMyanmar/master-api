package com.sdm.auth.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivateRequest implements Serializable {
    @NotBlank
    private String user;

    @NotBlank
    private String token;
}
