package com.sdm.core;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class DefaultReadWriteTest extends DefaultReadTest{
    protected abstract String suffix();
    protected abstract String getFakeCreate();
    protected abstract String getMultiFakeCreate();
    protected abstract String getFakeUpdate();
    protected abstract String getMultiFakeUpdate();

    @Test
    @Order(1)
    public void create() throws Exception{
        ResultActions result=this.mockMvc.perform(MockMvcRequestBuilders
            .post(getUrl())
            .contentType(MediaType.APPLICATION_JSON)
            .header("User-Agent", "SPRING_BOOT_TEST")
            .content(getFakeCreate()));

        String json=result.andReturn().getResponse().getContentAsString();
        Map<String, Object> map = objectMapper.readValue(json, Map.class);
        Map<String,Object> contentMap=(HashMap<String,Object>)map.get("content");

        System.setProperty(suffix()+"ID",contentMap.get("id").toString());

        result.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @Order(2)
    public void multiCreate() throws Exception{
        ResultActions result=this.mockMvc.perform(MockMvcRequestBuilders
                .post(getUrl()+"multi")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "SPRING_BOOT_TEST")
                .content(getMultiFakeCreate()));

        String json=result.andReturn().getResponse().getContentAsString();
        Map<String, Object> map = objectMapper.readValue(json, Map.class);
        Map<String,Object> contentMap=(HashMap<String,Object>)map.get("content");
        List<LinkedHashMap> linkedHashMaps=(ArrayList)contentMap.get("data");

        System.setProperty(suffix()+"ID2",linkedHashMaps.get(0).get("id").toString());
        System.setProperty(suffix()+"ID3",linkedHashMaps.get(1).get("id").toString());

        result.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @Order(3)
    public void update() throws Exception{
        ResultActions result=this.mockMvc.perform(MockMvcRequestBuilders
                .put(getUrl()+System.getProperty(suffix()+"ID"))
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "SPRING_BOOT_TEST")
                .content(getFakeUpdate()));

        result.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Order(4)
    public void multiUpdate() throws Exception{
        ResultActions result=this.mockMvc.perform(MockMvcRequestBuilders
                .put(getUrl()+"multi")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "SPRING_BOOT_TEST")
                .content(getMultiFakeUpdate()));

        result.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Order(5)
    public void remove() throws Exception{
        ResultActions result=this.mockMvc.perform(MockMvcRequestBuilders
                .delete(getUrl()+System.getProperty(suffix()+"ID"))
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "SPRING_BOOT_TEST"));

        result.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Order(6)
    public void multiRemove() throws Exception{
        Set<String> ids=new HashSet<>();
        ids.add(System.getProperty(suffix()+"ID2"));
        ids.add(System.getProperty(suffix()+"ID3"));

        ResultActions result=this.mockMvc.perform(MockMvcRequestBuilders
                .delete(getUrl()+"multi/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "SPRING_BOOT_TEST")
                .content(objectMapper.writeValueAsString(ids)));

        result.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
