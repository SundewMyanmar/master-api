package com.sdm.core.model.facebook;

import com.sdm.core.model.facebook.type.AttachmentType;

import org.json.JSONObject;

/**
 * The Messenger Platform allows you to attach assets to messages, including
 * audio, video, images, and files. Max attachment size is 25MB. There are three
 * ways to attach an asset to a message: URL, FILE, Attachment_ID
 */
public class AttachmentBuilder extends MessageBuilder {
    private AttachmentType type;
    private JSONObject payload;

    public AttachmentBuilder() {
        super();
        this.payload = new JSONObject();
    }

    @Override
    public JSONObject build() {
        JSONObject attachment = new JSONObject();
        attachment.put("type", type.toString());
        attachment.put("payload", this.payload);
        this.getMessage().put("attachment", this.payload);
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
        this.payload = new JSONObject();
        this.payload.put("url", url);
        this.payload.put("reusable", reusable);
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
        this.payload = new JSONObject();
        this.payload.put("attachment_id", id);
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
    public JSONObject getPayload() {
        return payload;
    }

    /**
     * @param payload the payload to set
     */
    public void setPayload(JSONObject payload) {
        this.payload = payload;
    }

}