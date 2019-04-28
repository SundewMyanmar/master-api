package com.sdm.core.model.facebook;

import com.google.gson.JsonObject;
import com.sdm.core.model.facebook.type.AttachmentType;


/**
 * The Messenger Platform allows you to attach assets to messages, including
 * audio, video, images, and files. Max attachment size is 25MB. There are three
 * ways to attach an asset to a message: URL, FILE, Attachment_ID
 */
public class AttachmentBuilder extends MessageBuilder {
    private AttachmentType type;
    private JsonObject payload;

    public AttachmentBuilder() {
        super();
        this.payload = new JsonObject();
    }

    @Override
    public JsonObject build() {
        JsonObject attachment = new JsonObject();
        attachment.addProperty("type", type.toString());
        attachment.add("payload", this.payload);
        this.getMessage().add("attachment", this.payload);
        return super.build();
    }

    /**
     * To send an attachment from a URL
     * 
     * @param url
     * @param reusable
     * @return
     */
    public void attachURL(String url, boolean reusable) {
        this.payload = new JsonObject();
        this.payload.addProperty("url", url);
        this.payload.addProperty("reusable", reusable);
    }

    /**
     * The Messenger Platform supports saving assets via the Send API and Attachment
     * Upload API. This allows you reuse assets, rather than uploading them every
     * time they are needed.
     * 
     * @param id
     * @return
     */
    public void attachAssets(String id) {
        this.payload = new JsonObject();
        this.payload.addProperty("attachment_id", id);
    }

    /**
     * @return the type
     */
    public AttachmentType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(AttachmentType type) {
        this.type = type;
    }

    /**
     * @return the payload
     */
    public JsonObject getPayload() {
        return payload;
    }

    /**
     * @param payload the payload to set
     */
    public void setPayload(JsonObject payload) {
        this.payload = payload;
    }

}