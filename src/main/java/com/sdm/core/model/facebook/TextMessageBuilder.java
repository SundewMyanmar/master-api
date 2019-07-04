package com.sdm.core.model.facebook;

import com.google.gson.JsonObject;

public class TextMessageBuilder extends MessageBuilder {

    private String text;

    public TextMessageBuilder() {
        super();
    }

    public TextMessageBuilder(String text) {
        super();
        this.text = text;
    }

    @Override
    public JsonObject build() {
        this.getMessage().addProperty("text", this.text);
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