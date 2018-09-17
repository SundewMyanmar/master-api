/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Htoonlin
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(value = {"title", "message", "details"})
public class MessageModel implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -459150304625745739L;

    @JsonIgnore
    private HttpStatus status;
    private String title;
    private String message;
    private Map<String, Object> details;

    public MessageModel(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public static synchronized MessageModel createMessage(HttpStatus status, String title, String message) {
        MessageModel instance = new MessageModel(status, message);
        instance.setTitle(title);
        return instance;
    }

    public static synchronized MessageModel createMessage(String title, String message) {
        MessageModel instance = new MessageModel(HttpStatus.OK, message);
        instance.setTitle(title);
        return instance;
    }

    public static synchronized MessageModel createWithDetail(String message, Map<String, Object> details) {
        MessageModel instance = new MessageModel(HttpStatus.OK, message);
        instance.setDetails(details);
        return instance;
    }

    public static synchronized MessageModel createWithDetail(HttpStatus status, String message, Map<String, Object> details) {
        MessageModel instance = new MessageModel(status, message);
        instance.setDetails(details);
        return instance;
    }

    public String getTitle() {
        if (title == null || title.isEmpty()) {
            return this.status.getReasonPhrase();
        }
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}
