/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.master.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Htoonlin
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RegistrationRequest implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -6887685772544814783L;

    @NotBlank
    @Email
    @Size(min = 6, max = 255)
    private String email;

    @NotBlank
    @Pattern(regexp = "[a-zA-Z0-9_\\.]+",
        message = "Sorry! invalid user name, allow char (a-zA-Z0-9) and special char (`.` and `_`). Eg./ mg_hla.09")
    @Size(max = 255)
    private String userName;


    @Size(max = 255)
    private String displayName;

    @NotBlank(message = "Password can't be blank.")
    @Size(min = 6, max = 255)
    private String password;

    @NotBlank(message = "DeviceID can't be blank.")
    @Size(max = 255)
    private String deviceId;

    @NotBlank(message = "Device OS can't be blank.")
    @Size(max = 50)
    private String deviceOS;

    @Size(max = 255)
    private String firebaseToken;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceOS() {
        return deviceOS;
    }

    public void setDeviceOS(String deviceOS) {
        this.deviceOS = deviceOS;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }

    public void setFirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RegistrationRequest other = (RegistrationRequest) obj;
        if (email == null) {
            if (other.email != null) {
                return false;
            }
        } else if (!email.equals(other.email)) {
            return false;
        }
        return true;
    }

}
