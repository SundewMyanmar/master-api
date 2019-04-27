package com.sdm.core.model.facebook;

import java.io.Serializable;
import org.json.JSONObject;

public interface FacebookSerialize extends Serializable {

    public void deserialize(JSONObject value);

    public JSONObject serialize();
}
