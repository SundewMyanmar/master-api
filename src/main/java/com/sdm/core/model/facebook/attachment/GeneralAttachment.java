package com.sdm.core.model.facebook.attachment;

import com.sdm.core.model.facebook.FacebookSerialize;
import com.sdm.core.model.facebook.type.AttachmentType;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneralAttachment implements FacebookSerialize {

    public static final Logger LOG = LoggerFactory.getLogger(GeneralAttachment.class);

    /**
     *
     */
    private static final long serialVersionUID = 5249453189678864653L;

    private String url;

    private String title;

    private AttachmentType type;

    @Override
    public JSONObject serialize() {
        JSONObject attachment = new JSONObject();
        try {
            if (this.type != null) {
                attachment.put("type", this.type.toString());
            }

            if (this.title != null && this.title.length() > 0) {
                attachment.put("title", this.title);
                if (this.url != null && this.url.length() > 0) {
                    attachment.put("url", this.url);
                }
            } else if (this.url != null && this.url.length() > 0) {
                attachment.put("payload", new JSONObject().put("url", this.url));
            }

        } catch (Exception ex) {
            LOG.warn(ex.getLocalizedMessage(), ex);
        }
        return attachment;
    }

    @Override
    public void deserialize(JSONObject value) {
        try {
            if (value.has("payload") && value.getJSONObject("payload").has("url")) {
                this.url = value.getJSONObject("payload").getString("url");
            } else if (value.has("url") && !value.isNull("url")) {
                this.url = value.getString("url");
                if (value.has("title")) {
                    this.title = value.getString("title");
                }
            }

            if (value.has("type")) {
                this.type = AttachmentType.valueOf(value.getString("type"));
            }
        } catch (Exception ex) {
            LOG.warn(ex.getLocalizedMessage(), ex);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public AttachmentType getType() {
        return type;
    }

    public void setType(AttachmentType type) {
        this.type = type;
    }

}
