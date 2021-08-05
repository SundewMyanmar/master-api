/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.auth.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Htoonlin
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest implements Serializable {

    @NotBlank(message = "User field can't be blank.")
    @Size(min = 6, max = 255)
    private String user;

    @NotBlank(message = "Password field can't be blank.")
    @Size(min = 6, max = 255)
    private String oldPassword;

    @NotBlank(message = "New password field can't be blank.")
    @Size(min = 6, max = 255)
    private String newPassword;
}
