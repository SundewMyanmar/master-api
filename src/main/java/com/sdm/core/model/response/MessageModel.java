/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Htoonlin
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(value = {"code", "message", "errors", "trace"})
public class MessageModel implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -459150304625745739L;

    private int code;
    private HttpStatus status;
    private String message;
    private Map<String, String> errors;
    private Map<String, Object> trace;

    public MessageModel() {
    }

    public MessageModel(HttpStatus status, String message) {
        this.status = status;
        this.code = status.value();
        this.message = message;
    }

    public static synchronized MessageModel createErrors(HttpStatus status, String message, Map<String, String> errors) {
        MessageModel instance = new MessageModel(status, message);
        instance.setErrors(errors);
        return instance;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
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

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    public Map<String, Object> getTrace() {
        return trace;
    }

    public void setTrace(Map<String, Object> trace) {
        this.trace = trace;
    }

}
