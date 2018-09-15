/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @param <T>
 * @author Htoonlin
 */
@JsonPropertyOrder(value = {"code", "status", "content", "timestamp"})
public class DefaultResponse<T extends Serializable> {

    @JsonIgnore
    private HttpStatus status;
    private Map<String, Object> headers;

    public DefaultResponse(HttpStatus status, T content) {
        this.status = status;
        this.content = content;
    }

    public DefaultResponse(T content) {
        // Get Code from message model
        this(HttpStatus.OK, content);
    }

    private T content;

    public void setContent(T content) {
        this.content = content;
    }

    public long getTimestamp() {
        return (new Date()).getTime();
    }

    @JsonGetter("code")
    public int getCode() {
        return status.value();
    }

    @JsonGetter("status")
    public String getStatus() {
        return this.status.series().name();
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public Object getContent() {
        return this.content;
    }
}
