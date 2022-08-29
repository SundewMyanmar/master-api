package com.sdm.auth.model.request;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author htoonlin
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnonymousRequest extends TokenInfo implements Serializable {

    private String brand;

    private String carrier;

    private String manufacture;

    @NotBlank
    @Size(max = 255)
    private String name;
}
