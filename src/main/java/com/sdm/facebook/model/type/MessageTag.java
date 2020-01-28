package com.sdm.facebook.model.type;

/**
 * Adding a tag to a message allows you to send it outside the 24+1 window, for a limited number of use cases, per Messenger Platform policy. Ref: https://developers.facebook.com/docs/messenger-platform/send-api-reference/tags
 * Ref => https://developers.facebook.com/docs/messenger-platform/send-messages/message-tags
 *
 * @author htoonlin
 */
public enum MessageTag {
    BUSINESS_PRODUCTIVITY,
    COMMUNITY_ALERT,
    CONFIRMED_EVENT_REMINDER,
    NON_PROMOTIONAL_SUBSCRIPTION,
    PAIRING_UPDATE,
    APPLICATION_UPDATE,
    ACCOUNT_UPDATE,
    PAYMENT_UPDATE,
    PERSONAL_FINANCE_UPDATE,
    SHIPPING_UPDATE,
    RESERVATION_UPDATE,
    ISSUE_RESOLUTION,
    APPOINTMENT_UPDATE,
    GAME_EVENT,
    TRANSPORTATION_UPDATE,
    FEATURE_FUNCTIONALITY_UPDATE,
    TICKET_UPDATE;
}
