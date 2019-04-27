package com.sdm.core.model.facebook.webhook;

import com.sdm.core.model.facebook.FacebookSerialize;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage following messages:
 * messages_deliveries => https://developers.facebook.com/docs/messenger-platform/reference/webhook-events/message-deliveries
 * message_reads => https://developers.facebook.com/docs/messenger-platform/reference/webhook-events/message-reads
 */
public class NotifyMessage implements FacebookSerialize {

    private static final Logger LOG = LoggerFactory.getLogger(NotifyMessage.class);

    /**
     *
     */
    private static final long serialVersionUID = -4431046390411955130L;

    /**
     * Array containing message IDs of messages that were delivered. Field may not be present.
     */
    private List<String> messageIds;

    /**
     * Messages are delivery or read.
     */
    private String status;

    /**
     * All messages that were sent before this timestamp were delivered.
     */
    private long watermark;

    /**
     * Sequence number
     */
    private int sequence;

    public NotifyMessage(String status) {
        this.status = status;
    }

    @Override
    public JSONObject serialize() {
        return new JSONObject(this);
    }

    @Override
    public void deserialize(JSONObject value) {
        try{
            if (value.has("watermark")) {
                this.watermark = value.getLong("watermark");
            }
            if (value.has("seq")) {
                this.sequence = value.getInt("seq");
            }
            if (value.has("mids")) {
                JSONArray ids = value.getJSONArray("mids");
                for (int i = 0; i < ids.length(); i++) {
                    this.addMessageId(ids.getString(i));
                }
            }
        }catch(Exception ex){
            LOG.warn(ex.getLocalizedMessage(), ex);
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void addMessageId(String id) {
        if (this.messageIds == null) {
            this.messageIds = new ArrayList<>();
        }

        this.messageIds.add(id);
    }

    public List<String> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(List<String> messageIds) {
        this.messageIds = messageIds;
    }

    public long getWatermark() {
        return watermark;
    }

    public void setWatermark(long watermark) {
        this.watermark = watermark;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

}
