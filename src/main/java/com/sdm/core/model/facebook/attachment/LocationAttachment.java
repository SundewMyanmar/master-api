package com.sdm.core.model.facebook.attachment;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocationAttachment extends GeneralAttachment {

    private static final Logger LOG = LoggerFactory.getLogger(LocationAttachment.class);
    /**
     *
     */
    private static final long serialVersionUID = 884178724612147977L;
    /**
     * cordinates.lat
     */
    private double latitude;

    /**
     * coordinates.long
     */
    private double longtitude;

    @Override
    public JSONObject serialize() {
        JSONObject attachment = super.serialize();
        try {
            attachment.put("type", "location");
            JSONObject cords = new JSONObject();
            cords.put("lat", this.latitude);
            cords.put("long", this.longtitude);
            attachment.put("payload", new JSONObject().put("coordinates", cords));
        } catch (Exception ex) {
            LOG.warn(ex.getLocalizedMessage(), ex);
        }

        return attachment;
    }

    @Override
    public void deserialize(JSONObject value) {
        try{
            if (value.has("payload") && value.getJSONObject("payload").has("coordinates")) {
                JSONObject cords = value.getJSONObject("payload").getJSONObject("coordinates");
                if (cords.has("lat")) {
                    this.latitude = cords.getDouble("lat");
                }
    
                if (cords.has("long")) {
                    this.latitude = cords.getDouble("long");
                }
            }
    
            super.deserialize(value);
        }catch(Exception ex){
            LOG.warn(ex.getLocalizedMessage(), ex);
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

}
