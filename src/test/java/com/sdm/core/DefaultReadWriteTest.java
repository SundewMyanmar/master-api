package com.sdm.core;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class DefaultReadWriteTest extends DefaultReadTest{
    protected abstract String suffix();
    protected abstract String getFakeCreate();
    protected abstract String getFakeUpdate();

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

        System.setProperty(suffix()+"ID",map.get("id").toString());

        result.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @Order(2)
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
    @Order(3)
    public void remove() throws Exception{
        ResultActions result=this.mockMvc.perform(MockMvcRequestBuilders
                .delete(getUrl()+System.getProperty(suffix()+"ID"))
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "SPRING_BOOT_TEST"));

        result.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

//    @Test
//    @Order(4)
//    public void multiRemove() throws Exception{
//        Set<String> ids=new HashSet<>();
//        ids.add(System.getProperty(suffix()+"ID2"));
//        ids.add(System.getProperty(suffix()+"ID3"));
//
//        ResultActions result=this.mockMvc.perform(MockMvcRequestBuilders
//                .delete(getUrl()+"multi/")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("User-Agent", "SPRING_BOOT_TEST")
//                .content(objectMapper.writeValueAsString(ids)));
//
//        result.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isOk());
//    }
}
