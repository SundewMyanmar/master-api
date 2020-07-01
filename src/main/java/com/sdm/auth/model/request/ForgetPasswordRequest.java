package com.sdm.auth.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForgetPasswordRequest {
    @NotBlank
    @Size(min = 5, max = 50)
    private String phoneNumber;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    private String callback;
}
