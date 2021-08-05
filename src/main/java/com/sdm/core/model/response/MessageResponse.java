/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.model.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sdm.core.util.Globalizer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Htoonlin
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(value = {"code", "title", "message", "details"})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse implements Serializable {

    @JsonIgnore
    private HttpStatus status;
    private String title;
    private String message;
    private Map<String, Object> details;

    public MessageResponse(String message) {
        this.status = HttpStatus.OK;
        this.message = message;
    }

    public MessageResponse(String title, String message) {
        this(message);
        this.title = title;
    }

    public MessageResponse(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @JsonGetter("code")
    public String getCode() {
        if (status == null) {
            return "UNKNOWN";
        }

        return "HTTP_" + status.value();
    }

    public String getTitle() {
        if (Globalizer.isNullOrEmpty(title)) {
            return this.status.getReasonPhrase();
        }
        return this.title;
    }
}
