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
    protected abstract String getOwner();

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
    public void getByIdAndOwner()throws Exception{
        ResultActions result =this.mockMvc.perform(MockMvcRequestBuilders
                .get(getUrl()+"owner/"+getDefaultId())
                .param("owner",getOwner())
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", Constants.TEST_CASE.userAgent));
        String json=result.andReturn().getResponse().getContentAsString();
                result.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
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

    @Test
    public void getPageByPageByOwner()throws Exception{
        this.mockMvc.perform(MockMvcRequestBuilders
                .get(getUrl()+"owner")
                .param("owner",getOwner())
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", Constants.TEST_CASE.userAgent))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isPartialContent());
    }

    @Test
    public void GetAllByOwner()throws Exception{
        this.mockMvc.perform(MockMvcRequestBuilders
                .get(getUrl()+"allData/owner")
                .param("owner",getOwner().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", Constants.TEST_CASE.userAgent))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
