/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.model.response;

import com.sdm.core.util.Globalizer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author htoonlin
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteParamResponse implements Serializable {

    private String defaultValue;
    private String type;
    private String paramType;

    public boolean isRequire() {
        return Globalizer.isNullOrEmpty(defaultValue);
    }

}
