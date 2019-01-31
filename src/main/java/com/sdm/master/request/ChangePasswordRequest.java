/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.master.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Htoonlin
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ChangePasswordRequest implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6890755299063523487L;
    private String user;

    @NotBlank(message = "User field can't be blank.")
    @Size(min = 4, max = 255)
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    private String oldPassword;

    @NotBlank(message = "Password field can't be blank.")
    @Size(min = 6, max = 255)
    public String getOldPassword() {
        return this.oldPassword;
    }

    public void setOldPassword(String value) {
        this.oldPassword = value;
    }

    private String newPassword;

    @NotBlank(message = "New password field can't be blank.")
    @Size(min = 6, max = 255)
    public String getNewPassword() {
        return this.newPassword;
    }

    public void setNewPassword(String value) {
        this.newPassword = value;
    }

}
