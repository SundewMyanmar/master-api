/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.model.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Htoonlin
 */
@JsonPropertyOrder(value = {"path", "method", "responseType", "queryParams,", "entityParams", "formParams", "resourceClass", "resourceMethod"})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RouteResponse implements Serializable {

    private String resourceClass;
    private String resourceMethod;
    private String path;
    private String method;
    private String responseType;
    private Map<String, RouteParamResponse> queryParams;
    private Map<String, RouteParamResponse> otherParams;

    public void addQueryParam(String name, RouteParamResponse body) {
        if (this.queryParams == null) {
            this.queryParams = new HashMap<>();
        }
        this.queryParams.put(name, body);
    }

    public void addOtherParam(String name, RouteParamResponse body) {
        if (this.otherParams == null) {
            this.otherParams = new HashMap<>();
        }
        this.otherParams.put(name, body);
    }

}
