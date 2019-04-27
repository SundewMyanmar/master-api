package com.sdm.core.model.facebook;

import org.json.JSONObject;

public class TextMessageBuilder extends MessageBuilder {

    private String text;

    public TextMessageBuilder(){
        super();
    }

    public TextMessageBuilder(String text){
        super();
        this.text = text;
    }

    @Override
    public JSONObject build() {
        this.getMessage().put("text", this.text);
        return super.build();
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }
    
}