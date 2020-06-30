package com.sdm.core;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.Serializable;

public abstract class DefaultReadTest extends DefaultTest {
    protected abstract String getUrl();

    protected abstract Serializable getId();

    //READ CONTROLLER
    @Test
    @Order(1)
    public void getStructure() throws Exception {
        String url = getUrl() + "/struct";
        this.test(url, HttpMethod.GET, null)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Order(10)
    public void getPageByPage() throws Exception {
        String url = getUrl();
        this.test(url, HttpMethod.GET, null)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isPartialContent());
    }

    @Test
    @Order(11)
    public void getById() throws Exception {
        String url = getUrl() + "/" + getId();
        this.test(url, HttpMethod.GET, null)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Order(12)
    public void getAll() throws Exception {
        String url = getUrl() + "/all";
        this.test(url, HttpMethod.GET, null)
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
