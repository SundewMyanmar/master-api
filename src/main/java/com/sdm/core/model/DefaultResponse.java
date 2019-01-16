/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;

/**
 * @author Htoonlin
 */
@JsonPropertyOrder(value = {"code", "status", "content", "timestamp"})
public class DefaultResponse {

    private int code;
    private String status;
    private Object content;

    public DefaultResponse() {
    }

    public DefaultResponse(int code, String status, Object content) {
        this.code = code;
        this.status = status;
        this.content = content;
    }

    public long getTimestamp() {
        return (new Date()).getTime();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
