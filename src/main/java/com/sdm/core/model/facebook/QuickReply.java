package com.sdm.core.model.facebook;

import org.json.JSONObject;

public class QuickReply implements FacebookSerialize {

    /**
     *
     */
    private static final long serialVersionUID = -8274764356753923359L;

    private String type;
    private String title;
    private String payload;
    private String image;

    public QuickReply(){
        
    }

    public QuickReply(String type, String title){
        this.type = type;
        this.title = title;
    }

    @Override
    public void deserialize(JSONObject value) {
        if (value.has("content_type")) {
            this.type = value.getString("content_type");
        }

        if (value.has("title")) {
            this.title = value.getString("title");
        }

        if (value.has("payload")) {
            this.title = value.getString("payload");
        }

        if (value.has("image_url")) {
            this.title = value.getString("image_url");
        }
    }

    @Override
    public JSONObject serialize() {
        JSONObject quickReply = new JSONObject();
        if (this.type != null && this.type.length() > 0) {
            quickReply.put("content_type", this.type);
        }
        if (this.title != null && this.title.length() > 0) {
            quickReply.put("title", this.title);
        }
        if (this.payload != null && this.payload.length() > 0) {
            quickReply.put("payload", this.payload);
        }
        if (this.image != null && this.image.length() > 0) {
            quickReply.put("image_url", this.image);
        }
        return quickReply;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the payload
     */
    public String getPayload() {
        return payload;
    }

    /**
     * @param payload the payload to set
     */
    public void setPayload(String payload) {
        this.payload = payload;
    }

    /**
     * @return the image
     */
    public String getImage() {
        return image;
    }

    /**
     * @param image the image to set
     */
    public void setImage(String image) {
        this.image = image;
    }

    

}
