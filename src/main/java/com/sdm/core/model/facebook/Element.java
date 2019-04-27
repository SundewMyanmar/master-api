package com.sdm.core.model.facebook;

import org.json.JSONArray;
import org.json.JSONObject;

public class Element{
    
    /**
     * It describe instances of the generic template to be sent. Specifying multiple elements will send a horizontally scrollable carousel of templates. A maximum of 10 elements is supported.
     * @param title
     * @param number
     * @return
     */
    public static JSONObject genericOrList(String title, String subtitle, String image, JSONObject action, JSONArray buttons) {
        JSONObject element = new JSONObject();
        element.put("title", title);
        if(subtitle != null && subtitle.length() > 0){
            element.put("subtitle", subtitle);
        }

        if(image != null && image.length() > 0){
            element.put("image_url", image);
        }

        if(action != null){
            element.put("default_action", action);
        }

        if(buttons != null){
            element.put("buttons", buttons);
        }
        
        return element;
    }

    /**
     * It describe the media in the message. A maximum of 1 element is supported.
     * The type of media being sent - image or video is supported.
     * @param type
     * @param id
     * @param url
     * @param buttons
     * @return
     */
    public static JSONObject media(String type, String id, String url, JSONArray buttons){
        JSONObject element = new JSONObject();
        element.put("media_type", type);
        if(id != null && id.length() > 0){
            element.put("attachment_id", id);
        }

        if(url != null && url.length() > 0){
            element.put("url", url);
        }

        if(buttons != null){
            element.put("buttons", buttons);
        }
        
        return element;
    }

    /**
     * Array of maximum 1 object that describes the open graph object to display.
     * @param url
     * @param buttons
     * @return
     */
    public static JSONObject openGraph(String url, JSONArray buttons){
        JSONObject element = new JSONObject();
        element.put("url", url);

        if(buttons != null){
            element.put("buttons", buttons);
        }
        
        return element;
    }
}