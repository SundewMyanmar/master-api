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
public class AuthRequest implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -341416570638461653L;

    @NotBlank(message = "Please enter user name or email.")
    @Size(min = 4, max = 255)
    private String user;

    @NotBlank(message = "Password can't be blank.")
    @Size(min = 2, max = 255)
    private String password;


    @NotBlank(message = "DeviceID can't be blank.")
    @Size(max = 255)
    private String deviceId;

    @NotBlank(message = "Device OS can't be blank.")
    @Size(max = 50)
    private String deviceOS;

    @Size(max = 255)
    private String firebaseToken;

    public AuthRequest() {
    }

    public AuthRequest(String user, String password, String deviceId, String deviceOS, String firebaseToken) {
        this.user = user;
        this.password = password;
        this.deviceId = deviceId;
        this.deviceOS = deviceOS;
        this.firebaseToken = firebaseToken;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String value) {
        this.deviceId = value;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }


    public String getPassword() {
        return this.password;
    }

    public void setPassword(String value) {
        this.password = value;
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
        result = prime * result + ((deviceId == null) ? 0 : deviceId.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
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
        AuthRequest other = (AuthRequest) obj;
        if (deviceId == null) {
            if (other.deviceId != null) {
                return false;
            }
        } else if (!deviceId.equals(other.deviceId)) {
            return false;
        }
        if (user == null) {
            if (other.user != null) {
                return false;
            }
        } else if (!user.equals(other.user)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        return true;
    }

}
