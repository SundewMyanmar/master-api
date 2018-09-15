package com.sdm.master.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author htoonlin
 */
public class AnonymousRequest implements Serializable {

    private static final long serialVersionUID = 4618020126146257792L;


    @NotBlank(message = "Device UniqueID is required.")
    @Size(min = 6, max = 255)
    private String uniqueId;

    @NotBlank(message = "Device OS (ios, android, windows, browser) is required.")
    @Size(max = 50)
    private String os;

    //Firebase FCM registration token
    @NotBlank(message = "Firebase Token is required for notification.")
    @Size(max = 255)
    private String firebaseToken;

    @NotBlank(message = "Device User agent string is required.")
    private String userAgent;
    private String brand;
    private String carrier;
    private String manufacture;
    private String name;

    public AnonymousRequest() {
    }

    public AnonymousRequest(String uniqueId, String os, String firebaseToken, String userAgent) {
        this.uniqueId = uniqueId;
        this.os = os;
        this.firebaseToken = firebaseToken;
        this.userAgent = userAgent;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }

    public void setFirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getManufacture() {
        return manufacture;
    }

    public void setManufacture(String manufacture) {
        this.manufacture = manufacture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.uniqueId);
        hash = 67 * hash + Objects.hashCode(this.os);
        hash = 67 * hash + Objects.hashCode(this.firebaseToken);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AnonymousRequest other = (AnonymousRequest) obj;
        if (!Objects.equals(this.uniqueId, other.uniqueId)) {
            return false;
        }
        if (!Objects.equals(this.os, other.os)) {
            return false;
        }
        if (!Objects.equals(this.firebaseToken, other.firebaseToken)) {
            return false;
        }
        return true;
    }

}
