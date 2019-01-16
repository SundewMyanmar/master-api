/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Htoonlin
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(value = {"total", "count", "current_page", "data", "page_size", "page_count", "sorts"})
public class PaginationModel<T extends Serializable> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5020402341964835561L;
    private List<T> data;
    private long total;
    private int currentPage;
    private int pageSize;
    private String sorts;

    public PaginationModel() {
    }

    public PaginationModel(Page<T> page) {
        this.data = page.getContent();
        this.sorts = page.getPageable().getSort().toString();
        this.total = page.getTotalElements();
        this.pageSize = page.getPageable().getPageSize();
        this.currentPage = page.getPageable().getPageNumber();
    }

    public PaginationModel(List<T> data, long total, int currentPage, int pageSize) {
        this.data = data;
        this.total = total;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    public PaginationModel(List<T> data, long total, int currentPage, int pageSize, String sorts) {
        this.data = data;
        this.total = total;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.sorts = sorts;
    }

    public int getPageCount() {
        return (int) Math.ceil((double) total / pageSize);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCount() {
        return this.data.size();
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void addData(T entity) {
        if (this.data == null) {
            this.data = new ArrayList<>();
        }
        this.data.add(entity);
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public String getSorts() {
        return sorts;
    }

    public void setSorts(String sorts) {
        this.sorts = sorts;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + currentPage;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + pageSize;
        result = prime * result + (int) (total ^ (total >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PaginationModel other = (PaginationModel) obj;
        if (currentPage != other.currentPage) {
            return false;
        }
        if (data == null) {
            if (other.data != null) {
                return false;
            }
        } else if (!data.equals(other.data)) {
            return false;
        }
        if (pageSize != other.pageSize) {
            return false;
        }
        if (total != other.total) {
            return false;
        }
        return true;
    }
}
