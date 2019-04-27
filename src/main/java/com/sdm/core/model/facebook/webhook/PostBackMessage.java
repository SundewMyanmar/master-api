package com.sdm.core.model.facebook.webhook;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * messaging_postbacks =>
 * https://developers.facebook.com/docs/messenger-platform/reference/webhook-events/messaging_postbacks
 */
public class PostBackMessage extends ReferralMessage {
    private static final Logger LOG = LoggerFactory.getLogger(PostBackMessage.class);

    /**
     *
     */
    private static final long serialVersionUID = 1268975340559770781L;

    /**
     * Title for the CTA that was clicked on. This is sent to all apps subscribed to
     * the page. For apps other than the original CTA sender, the postback event
     * will be delivered via the standby channel.
     */
    private String title;

    /**
     * payload parameter that was defined with the button
     */
    private String payload;

    @Override
    public JSONObject serialize() {
        JSONObject postback = new JSONObject();
        try{
            postback.put("payload", this.payload);
            postback.put("referral", super.serialize());
        }catch(Exception ex){
            LOG.warn(ex.getLocalizedMessage(), ex);
        }
        return postback;
    }

    @Override
    public void deserialize(JSONObject value) {
        try{
            if (value.has("title")) {
                this.title = value.getString("title");
            }
            if (value.has("payload")) {
                this.payload = value.getString("payload");
            }
            if (value.has("referral")) {
                super.deserialize(value.getJSONObject("referral"));
            }
        }catch(Exception ex){
            LOG.warn(ex.getLocalizedMessage(), ex);
        }
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
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

}
