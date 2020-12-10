/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

/**
 * @author Htoonlin
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(value = {"total", "count", "currentPage", "data", "pageSize", "pageCount", "sort"})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginationResponse<T> implements Serializable {

    /**
     * Data items.
     */
    private List<T> data;
    /**
     * Total number of Items.
     */
    private long total;
    /**
     * Current page number.
     */
    private int currentPage;
    /**
     * Number of items per page.
     */
    private int pageSize;
    /**
     * Sortable Data Info
     */
    private String sort;

    public PaginationResponse(Page<T> page) {
        this.data = page.getContent();
        this.sort = page.getPageable().getSort().toString();
        this.total = page.getTotalElements();
        this.pageSize = page.getPageable().getPageSize();
        this.currentPage = page.getPageable().getPageNumber();
    }
}
