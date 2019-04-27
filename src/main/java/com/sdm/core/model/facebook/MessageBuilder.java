package com.sdm.core.model.facebook;

import com.sdm.core.model.facebook.type.NotificationType;
import com.sdm.core.model.facebook.type.SenderAction;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Send Message Format Ref =>
 * https://developers.facebook.com/docs/messenger-platform/reference/send-api
 */
public class MessageBuilder {
    private String messagingType;
    private JSONObject recipient;
    private JSONObject message;
    private JSONArray quickReplies;
    private NotificationType notificationType;
    private String tag;

    public MessageBuilder() {
        this.recipient = new JSONObject();
        this.message = new JSONObject();
    }

    public JSONObject build() {
        JSONObject result = new JSONObject();
        result.put("recipient", this.recipient);
        result.put("message", this.message);

        if (quickReplies != null && quickReplies.length() > 0) {
            this.message.put("quick_replies", this.quickReplies);
        }

        if (this.messagingType != null && this.messagingType.length() > 0) {
            result.put("messaging_type", this.messagingType);
        }

        if (this.notificationType != null) {
            result.put("notification_type", this.notificationType.toString());
        }

        if (this.tag != null && this.tag.length() > 0) {
            result.put("messaging_type", "MESSAGE_TAG");
            result.put("tag", this.tag);
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
    public JSONObject buildAction(SenderAction action) {
        JSONObject result = this.build();
        if (action != null) {
            result.put("sender_action", action.toString());
        }
        return result;
    }

    /**
     * 
     * @param id
     * @return
     */
    public void setRecipient(String id) {
        this.setRecipient(id, null, null, null, null);
    }

    /**
     * 
     * @param id
     * @param phone
     * @param userRef
     * @param firstName
     * @param lastName
     * @return
     */
    public void setRecipient(String id, String phone, String userRef, String firstName, String lastName) {
        this.recipient.put("id", id);

        if (phone != null && phone.length() > 0) {
            this.recipient.put("phone", phone);
        }

        if (userRef != null && userRef.length() > 0) {
            this.recipient.put("user_ref", userRef);
        }

        if (firstName != null && firstName.length() > 0 && lastName != null && lastName.length() > 0) {
            JSONObject name = new JSONObject();
            name.put("first_name", firstName);
            name.put("last_name", lastName);
            this.recipient.put("name", name);
        }
    }

    /**
     * @return the recipient
     */
    public JSONObject getRecipient() {
        return recipient;
    }

    /**
     * @param recipient the recipient to set
     */
    public void setRecipient(JSONObject recipient) {
        this.recipient = recipient;
    }

    /**
     * @return the message
     */
    public JSONObject getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(JSONObject message) {
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
    public String getMessagingType() {
        return messagingType;
    }

    /**
     * @param messagingType the messagingType to set
     */
    public void setMessagingType(String messagingType) {
        this.messagingType = messagingType;        
    }

    /**
     * Quick Replies provide a way to present buttons to the user in response to a
     * message.s
     *
     * @param quickReply
     * @return
     */
    public void addQuickReply(QuickReply quickReply) {
        this.quickReplies.put(quickReply.serialize());
    }

    /**
     * Custom string that is delivered as a message echo.
     *
     * @param metaData
     * @return
     */
    public void setMetaData(String metaData) {
        this.message.put("metadata", metaData);
    }
}