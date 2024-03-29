/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.auth.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Htoonlin
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest extends TokenInfo implements Serializable {

    @NotBlank
    @Size(min = 5, max = 50)
    private String phoneNumber;

    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 255)
    private String displayName;

    @NotBlank(message = "Password can't be blank.")
    @Size(min = 2, max = 255)
    private String password;
}
