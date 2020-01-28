package com.sdm.facebook.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sdm.facebook.model.type.MessageType;
import com.sdm.facebook.model.type.NotificationType;
import com.sdm.facebook.model.type.QuickReplyType;
import com.sdm.facebook.model.type.SenderAction;

/**
 * Send Message Format Ref =>
 * https://developers.facebook.com/docs/messenger-platform/reference/send-api
 */
public class MessageBuilder {
    private MessageType messagingType;
    private JsonObject recipient;
    private JsonObject message;
    private JsonArray quickReplies;

    private NotificationType notificationType;
    private String tag;

    public MessageBuilder() {
        this.recipient = new JsonObject();
        this.message = new JsonObject();
    }

    public JsonObject build() {
        JsonObject result = new JsonObject();
        result.add("recipient", this.recipient);

        result.add("message", this.message);

        if (quickReplies != null && quickReplies.size() > 0) {
            this.message.add("quick_replies", this.quickReplies);
        }

        if (this.messagingType != null) {
            result.addProperty("messaging_type", this.messagingType.toString());
        }

        if (this.notificationType != null) {
            result.addProperty("notification_type", this.notificationType.toString());
        }

        if (this.tag != null && this.tag.length() > 0) {
            result.addProperty("messaging_type", "MESSAGE_TAG");
            result.addProperty("tag", this.tag);
        }

        return result;
    }

    /**
     * Set typing indicators or send read receipts using the Send API, to let users
     * know you are processing their request.
     *
     * @param action
     * @return
     */
    public JsonObject buildAction(SenderAction action) {
        JsonObject result = this.build();
        if (action != null) {
            result.addProperty("sender_action", action.toString());
            result.remove("message");
        }
        return result;
    }

    /**
     * @param id
     * @return
     */
    public void setRecipient(String id) {
        this.setRecipient(id, null, null, null, null);
    }

    /**
     * @param id
     * @param phone
     * @param userRef
     * @param firstName
     * @param lastName
     * @return
     */
    public void setRecipient(String id, String phone, String userRef, String firstName, String lastName) {
        this.recipient.addProperty("id", id);

        if (phone != null && phone.length() > 0) {
            this.recipient.addProperty("phone", phone);
        }

        if (userRef != null && userRef.length() > 0) {
            this.recipient.addProperty("user_ref", userRef);
        }

        if (firstName != null && firstName.length() > 0 && lastName != null && lastName.length() > 0) {
            JsonObject name = new JsonObject();
            name.addProperty("first_name", firstName);
            name.addProperty("last_name", lastName);
            this.recipient.add("name", name);
        }
    }

    /**
     * @return the recipient
     */
    public JsonObject getRecipient() {
        return recipient;
    }

    /**
     * @param recipient the recipient to set
     */
    public void setRecipient(JsonObject recipient) {
        this.recipient = recipient;
    }

    /**
     * @return the message
     */
    public JsonObject getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(JsonObject message) {
        this.message = message;
    }

    /**
     * @return the notificationType
     */
    public NotificationType getNotificationType() {
        return notificationType;
    }

    /**
     * @param notificationType the notificationType to set
     */
    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    /**
     * @return the tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * @param tag the tag to set
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * @return the messagingType
     */
    public MessageType getMessagingType() {
        return messagingType;
    }

    /**
     * @param messagingType the messagingType to set
     */
    public void setMessagingType(MessageType messagingType) {
        this.messagingType = messagingType;
    }

    /**
     * Quick replies provide a way to present a set of up to 11 buttons
     * in-conversation that contain a title and optional image, and appear
     * prominently above the composer. You can also use quick replies to request a
     * person's location, email address, and phone number.
     * Ref => https://developers.facebook.com/docs/messenger-platform/reference/send-api/quick-replies
     *
     * @param type
     * @param title
     * @param payload
     * @param image
     */
    public void addQuickReply(QuickReplyType type, String title, String payload, String image) {
        if (this.quickReplies == null) {
            this.quickReplies = new JsonArray();
        }

        JsonObject quickReply = new JsonObject();
        if (type != null) {
            quickReply.addProperty("content_type", type.toString());
        }
        if (title != null && title.length() > 0) {
            quickReply.addProperty("title", title);
        }
        if (payload != null && payload.length() > 0) {
            quickReply.addProperty("payload", payload);
        }
        if (image != null && image.length() > 0) {
            quickReply.addProperty("image_url", image);
        }
        this.quickReplies.add(quickReply);
    }

    /**
     * Custom string that is delivered as a message echo.
     *
     * @param metaData
     * @return
     */
    public void setMetaData(String metaData) {
        this.message.addProperty("metadata", metaData);
    }
}