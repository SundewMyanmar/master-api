/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.model.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Htoonlin
 */
@JsonPropertyOrder({"count", "data"})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListResponse<T> implements Serializable {

    private Collection<T> data = new ArrayList<>();

    public int getCount() {
        return this.data.size();
    }

    public void addData(T entity) {
        if (this.data == null) {
            this.data = new ArrayList<>();
        }
        this.data.add(entity);
    }
}
