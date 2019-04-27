package com.sdm.core.model.facebook;

import com.sdm.core.model.facebook.type.TemplateType;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The Messenger Platform allows you to attach assets to messages, including
 * audio, video, images, and files. Max attachment size is 25MB. There are three
 * ways to attach an asset to a message: URL, FILE, Attachment_ID
 */
public class TemplateBuilder extends MessageBuilder {
    private JSONObject payload;

    public TemplateBuilder() {
        super();
        this.payload = new JSONObject();
    }

    public TemplateBuilder(TemplateType type){
        this();
        this.setTemplateType(type);
    }

    public void setTemplateType(TemplateType type) {
        this.payload.put("template_type", type);
    }

    @Override
    public JSONObject build() {
        JSONObject attachment = new JSONObject();
        attachment.put("type", "template");
        attachment.put("payload", this.payload);
        this.getMessage().put("attachment", attachment);
        return super.build();
    }

    /**
     * The button template allows you to send a structured message that includes
     * text and buttons Ref =>
     * https://developers.facebook.com/docs/messenger-platform/reference/template/button
     * 
     * @param text
     * @param buttons
     * @param sharable
     * @return
     */
    public JSONObject buildButtonTemplate(String text, JSONArray buttons, boolean sharable) {
        this.setTemplateType(TemplateType.button);
        this.payload.put("text", text);
        this.payload.put("buttons", buttons);

        if (sharable) {
            this.payload.put("sharable", sharable);
        }

        return this.build();
    }

    /**
     * The generic template allows you to send a structured message that includes an
     * image, text and buttons. A generic template with multiple templates described
     * in the elements array will send a horizontally scrollable carousel of items,
     * each composed of an image, text and buttons. Ref =>
     * https://developers.facebook.com/docs/messenger-platform/reference/template/generic
     * 
     * @param elements
     * @param sharable
     * @param imageRatio
     * @return
     */
    public JSONObject buildGenericTemplate(JSONArray elements, boolean sharable, String imageRatio) {
        this.setTemplateType(TemplateType.generic);
        this.payload.put("elements", elements);

        if (sharable) {
            this.payload.put("sharable", sharable);
        }

        if (imageRatio != null && imageRatio.length() > 0) {
            this.payload.put("image_aspect_ratio", imageRatio);
        }

        return this.build();
    }

    /**
     * The list template allows you to send a structured message with a set of items
     * rendered vertically. Ref =>
     * https://developers.facebook.com/docs/messenger-platform/reference/template/list
     * 
     * @param elements
     * @param buttons
     * @param topElementStyle
     * @param sharable
     * @return
     */
    public JSONObject buildListTemplate(JSONArray elements, JSONArray buttons, String topElementStyle,
            boolean sharable) {
        this.setTemplateType(TemplateType.list);
        this.payload.put("elements", elements);

        if (buttons != null) {
            this.payload.put("buttons", buttons);
        }

        if (sharable) {
            this.payload.put("sharable", sharable);
        }

        if (topElementStyle != null && topElementStyle.length() > 0) {
            this.payload.put("top_element_style", topElementStyle);
        }

        return this.build();
    }

    /**
     * The media template allows you to send a structured message that includes an
     * image or video, and an optional button. Ref =>
     * https://developers.facebook.com/docs/messenger-platform/reference/template/media
     * 
     * @param elements
     * @param sharable
     * @return
     */
    public JSONObject buildMediaTemplate(JSONArray elements, boolean sharable) {
        this.setTemplateType(TemplateType.media);
        this.payload.put("elements", elements);

        if (sharable) {
            this.payload.put("sharable", sharable);
        }

        return this.build();
    }

    /**
     * The Open Graph template allows you to send a structured message with an open
     * graph URL, plus an optional button. Currently, only sharing songs is
     * supported. The song will appear in a bubble that allows the message recipient
     * to see album art, and preview the song. Ref =>
     * https://developers.facebook.com/docs/messenger-platform/reference/template/open-graph
     * 
     * @param elements
     * @return
     */
    public JSONObject buildOpenGraphTemplate(JSONArray elements) {
        this.setTemplateType(TemplateType.open_graph);
        this.payload.put("elements", elements);

        return this.build();
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