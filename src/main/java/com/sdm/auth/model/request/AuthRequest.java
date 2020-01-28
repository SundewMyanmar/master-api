/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.auth.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -341416570638461653L;

    @NotBlank(message = "Please enter phone number or email.")
    @Size(min = 6, max = 255)
    private String user;

    @NotBlank(message = "Password can't be blank.")
    @Size(min = 2, max = 255)
    private String password;

    @NotBlank(message = "Device UniqueID is required.")
    @Size(max = 255)
    private String deviceId;

    @NotBlank(message = "Device OS (ios, android, windows, browser) is required.")
    @Size(max = 50)
    private String deviceOS;

    @Size(max = 255)
    private String firebaseToken;

}
