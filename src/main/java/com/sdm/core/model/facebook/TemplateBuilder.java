package com.sdm.core.model.facebook;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sdm.core.model.facebook.type.TemplateType;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * The Messenger Platform allows you to attach assets to messages, including
 * audio, video, images, and files. Max attachment size is 25MB. There are three
 * ways to attach an asset to a message: URL, FILE, Attachment_ID
 */
public class TemplateBuilder extends MessageBuilder {
    private JsonObject payload;

    public TemplateBuilder() {
        super();
        this.payload = new JsonObject();
    }

    public TemplateBuilder(TemplateType type) {
        this();
        this.setTemplateType(type);
    }

    public void setTemplateType(TemplateType type) {
        this.payload.addProperty("template_type", type.toString());
    }

    @Override
    public JsonObject build() {
        JsonObject attachment = new JsonObject();
        attachment.addProperty("type", "template");
        attachment.add("payload", this.payload);
        this.getMessage().add("attachment", attachment);
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
    public JsonObject buildButtonTemplate(String text, JsonArray buttons, boolean sharable) {
        this.setTemplateType(TemplateType.button);
        this.payload.addProperty("text", text);
        this.payload.add("buttons", buttons);

        if (sharable) {
            this.payload.addProperty("sharable", sharable);
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
    public JsonObject buildGenericTemplate(JsonArray elements, boolean sharable, String imageRatio) {
        this.setTemplateType(TemplateType.generic);
        this.payload.add("elements", elements);

        if (sharable) {
            this.payload.addProperty("sharable", sharable);
        }

        if (imageRatio != null && imageRatio.length() > 0) {
            this.payload.addProperty("image_aspect_ratio", imageRatio);
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

    public JsonObject buildReceiptTemplate(String recipient_name, String order_number, String currency, String payment_method, String order_url,
                                           JsonObject address, JsonObject summary, JsonArray adjustments, JsonArray elements) {
        this.setTemplateType(TemplateType.receipt);
        if (recipient_name != null && !recipient_name.equals("")) {
            this.payload.addProperty("recipient_name", recipient_name);
        }

        if (order_number != null && !order_number.equals("")) {
            this.payload.addProperty("order_number", order_number);
        }

        if (currency != null && !currency.equals("")) {
            this.payload.addProperty("currency", currency);
        }

        if (payment_method != null && !payment_method.equals("")) {
            this.payload.addProperty("payment_method", payment_method);
        }

        if (order_url != null && !order_url.equals("")) {
            this.payload.addProperty("order_url", order_url);
        }

        if (address != null) {
            this.payload.add("address", address);
        }

        if (summary != null) {
            this.payload.add("summary", summary);
        }

        if (adjustments != null && adjustments.size() > 0) {
            this.payload.add("adjustments", adjustments);
        }

        if (elements != null && elements.size() > 0) {
            this.payload.add("elements", elements);
        }

        //Set POSIX_TIMESTAMP
        //http://zetcode.com/java/unixtime/
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Rangoon"));
        calendar.setTime(new Date());

        this.payload.addProperty("timestamp", calendar.getTimeInMillis() / 1000L);

        return this.build();
    }

    public JsonObject buildListTemplate(JsonArray elements, JsonArray buttons, String topElementStyle,
                                        boolean sharable) {
        this.setTemplateType(TemplateType.list);
        this.payload.add("elements", elements);

        if (buttons != null) {
            this.payload.add("buttons", buttons);
        }

        if (sharable) {
            this.payload.addProperty("sharable", sharable);
        }

        if (topElementStyle != null && topElementStyle.length() > 0) {
            this.payload.addProperty("top_element_style", topElementStyle);
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
    public JsonObject buildMediaTemplate(JsonArray elements, boolean sharable) {
        this.setTemplateType(TemplateType.media);
        this.payload.add("elements", elements);

        if (sharable) {
            this.payload.addProperty("sharable", sharable);
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
    public JsonObject buildOpenGraphTemplate(JsonArray elements) {
        this.setTemplateType(TemplateType.open_graph);
        this.payload.add("elements", elements);

        return this.build();
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

    public static JsonObject buildWelcomeScreen(String greetingText1, String greetingText2) {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        JsonObject greet1 = new JsonObject();
        greet1.addProperty("locale", "default");
        greet1.addProperty("text", greetingText1);

        JsonObject greet2 = new JsonObject();
        greet2.addProperty("locale", "en_US");
        greet2.addProperty("text", greetingText2);

        jsonArray.add(greet1);
        jsonArray.add(greet2);

        jsonObject.add("greeting", jsonArray);
        return jsonObject;
    }
}