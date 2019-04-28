package com.sdm.core.model.facebook;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Element{
    
    /**
     * It describe instances of the generic template to be sent. Specifying multiple elements will send a horizontally scrollable carousel of templates. A maximum of 10 elements is supported.
     * @param title
     * @param number
     * @return
     */
    public static JsonObject genericOrList(String title, String subtitle, String image, JsonObject action, JsonArray buttons) {
        JsonObject element = new JsonObject();
        element.addProperty("title", title);
        if(subtitle != null && subtitle.length() > 0){
            element.addProperty("subtitle", subtitle);
        }

        if(image != null && image.length() > 0){
            element.addProperty("image_url", image);
        }

        if(action != null){
            element.add("default_action", action);
        }

        if(buttons != null){
            element.add("buttons", buttons);
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
    public static JsonObject media(String type, String id, String url, JsonArray buttons){
        JsonObject element = new JsonObject();
        element.addProperty("media_type", type);
        if(id != null && id.length() > 0){
            element.addProperty("attachment_id", id);
        }

        if(url != null && url.length() > 0){
            element.addProperty("url", url);
        }

        if(buttons != null){
            element.add("buttons", buttons);
        }
        
        return element;
    }

    /**
     * Array of maximum 1 object that describes the open graph object to display.
     * @param url
     * @param buttons
     * @return
     */
    public static JsonObject openGraph(String url, JsonArray buttons){
        JsonObject element = new JsonObject();
        element.addProperty("url", url);

        if(buttons != null){
            element.add("buttons", buttons);
        }
        
        return element;
    }
}