package com.sdm.core.model.facebook.webhook;

import com.sdm.core.model.facebook.FacebookSerialize;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * messaging_referrals =>
 * https://developers.facebook.com/docs/messenger-platform/reference/webhook-events/messaging_referrals
 * 
 * @author htoonlin
 */
public class ReferralMessage implements FacebookSerialize {

    private static final Logger LOG = LoggerFactory.getLogger(ReferralMessage.class);

    /**
     *
     */
    private static final long serialVersionUID = -5085297294538643176L;

    private String source;
    private String type;
    private String ref;
    private String refUrl;
    private String adId;

    @Override
    public JSONObject serialize() {
        return new JSONObject(this);
    }

    @Override
    public void deserialize(JSONObject value) {
        try{
            if (value.has("source")) {
                this.source = value.getString("source");
            }
    
            if (value.has("type")) {
                this.type = value.getString("type");
            }
    
            if (value.has("ref")) {
                this.ref = value.getString("ref");
            }
    
            if (value.has("referer_uri")) {
                this.refUrl = value.getString("referer_uri");
            }
    
            if (value.has("ad_id")) {
                this.adId = value.getString("ad_id");
            }
        }catch(Exception ex){
            LOG.warn(ex.getLocalizedMessage());
        }
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    /**
     * @return the refUrl
     */
    public String getRefUrl() {
        return refUrl;
    }

    /**
     * @param refUrl the refUrl to set
     */
    public void setRefUrl(String refUrl) {
        this.refUrl = refUrl;
    }

}
