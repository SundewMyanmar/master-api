package com.sdm.auth.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author htoonlin
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnonymousRequest extends AuthRequest implements Serializable {

    private String brand;

    private String carrier;

    private String manufacture;

    @NotBlank
    @Size(max = 255)
    private String name;
}
