package com.sdm.master.request;

import java.io.Serializable;

/**
 * @author htoonlin
 */
public class AnonymousRequest extends AuthRequest implements Serializable {

    private static final long serialVersionUID = 4618020126146257792L;

    private String brand;

    private String carrier;

    private String manufacture;

    private String name;

    public AnonymousRequest() {
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
}
