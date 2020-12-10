package com.sdm.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.util.Globalizer;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class DefaultReadWriteTest extends DefaultReadTest {
    protected static Map<String, Object> currentData;
    protected static List<Serializable> importedIds;

    @BeforeAll
    public static void init() {
        currentData = new LinkedHashMap<>();
        importedIds = new ArrayList<>();
        DefaultTest.init();
    }

    @AfterAll
    public static void close() {
        currentData = null;
        importedIds = null;
        DefaultTest.close();
    }

    protected abstract Map<String, Object> createFakeData();

    protected abstract Map<String, Object> updateFakeData();

    protected abstract Map<String, Object> partialUpdateFakeData();

    @Override
    protected Serializable getId() {
        if (!currentData.containsKey("id")) {
            return null;
        }
        return (Serializable) currentData.get("id");
    }

    protected int getVersion() {
        return (int) currentData.getOrDefault("version", -1);
    }

    protected void setData(ResultActions result) throws JsonProcessingException, UnsupportedEncodingException {
        String json = result.andReturn().getResponse().getContentAsString();
        currentData = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
        });
        importedIds.add(getId());
    }

    protected List<Serializable> skipRemoveIds() {
        return new ArrayList<>();
    }

    protected int getImportCount() {
        return 10;
    }

    protected List<Serializable> removableIds() {
        return importedIds.stream()
                .filter(id -> !skipRemoveIds().contains(id))
                .collect(Collectors.toList());
    }

    @Test
    @Order(2)
    public void create() throws Exception {
        currentData = this.createFakeData();
        String url = this.getUrl();
        ResultActions result = this.test(url, HttpMethod.POST, currentData);
        this.setData(result);

        //Check Inserted Version must be zero.
        Assertions.assertEquals(getVersion(), 0);
        Assertions.assertFalse(Globalizer.isNullOrEmpty(getId()));

        result.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @Order(3)
    public void update() throws Exception {
        var request = this.updateFakeData();
        String url = getUrl() + "/" + getId();
        ResultActions result = this.test(url, HttpMethod.PUT, request);
        this.setData(result);

        //Check
        Assertions.assertTrue(getVersion() > (int) request.get("version"));

        result.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Order(4)
    public void partialUpdate() throws Exception {
        Map<String, Object> request = this.partialUpdateFakeData();
        String url = getUrl() + "/" + getId();

        ResultActions result = this.test(url, HttpMethod.PATCH, request);
        this.setData(result);

        //Check Version
        Assertions.assertTrue(1 < getVersion());

        result.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Order(5)
    public void importData() throws Exception {
        List<Object> request = new ArrayList<>();
        for (int i = 0; i < getImportCount(); i++) {
            request.add(createFakeData());
        }
        String url = getUrl() + "/import";
        ResultActions result = this.test(url, HttpMethod.POST, request);
        String json = result.andReturn().getResponse().getContentAsString();
        ListResponse<LinkedHashMap> response = objectMapper.readValue(json, ListResponse.class);

        //Store Imported Ids to Remove
        response.getData().forEach(map -> {
            if (map.containsKey("id")) {
                importedIds.add((Serializable) map.get("id"));
            }
        });

        Assertions.assertTrue(importedIds.size() > 1);

        result.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }


    @Test
    @Order(100)
    public void remove() throws Exception {
        if (removableIds() == null && removableIds().size() <= 0) {
            return;
        }
        Serializable id = removableIds().get(0);
        String url = getUrl() + "/" + id;
        this.test(url, HttpMethod.DELETE, null)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
        importedIds.removeIf(x -> x.equals(id));
    }

    @Test
    @Order(101)
    public void removeAll() throws Exception {
        String url = getUrl() + "/";
        this.test(url, HttpMethod.DELETE, removableIds())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
