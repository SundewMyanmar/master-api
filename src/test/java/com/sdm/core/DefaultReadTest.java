package com.sdm.core;

import com.sdm.Constants;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public abstract class DefaultReadTest extends DefaultTest {
    protected abstract String getUrl();
    protected abstract String getDefaultId();

    //READ CONTROLLER
    @Test
    public void getStructure()throws Exception{
        this.mockMvc.perform(MockMvcRequestBuilders
                .get(getUrl()+"struct")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", Constants.TEST_CASE.userAgent))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void getById()throws Exception{
        this.mockMvc.perform(MockMvcRequestBuilders
                .get(getUrl()+ getDefaultId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", Constants.TEST_CASE.userAgent))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void getPageByPage()throws Exception{
        this.mockMvc.perform(MockMvcRequestBuilders
                .get(getUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", Constants.TEST_CASE.userAgent))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isPartialContent());
    }

    @Test
    public void GetAll()throws Exception{
        this.mockMvc.perform(MockMvcRequestBuilders
                .get(getUrl()+"allData")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", Constants.TEST_CASE.userAgent))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
