package com.sdm.admin;

import com.sdm.core.DefaultReadWriteTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.Map;


public class SystemMenuControllerTest extends DefaultReadWriteTest {
    @Override
    protected Map<String, Object> createFakeData() {
        Map<String, Object> systemMenu = new HashMap<>();
        systemMenu.put("label", faker.book().genre());
        systemMenu.put("description", faker.lorem().paragraph());
        systemMenu.put("path", faker.internet().domainSuffix());
        systemMenu.put("icon", faker.animal().name());
        systemMenu.put("priority", faker.number().numberBetween(0, 10));
        systemMenu.put("divider", faker.bool().bool());
        return systemMenu;
    }

    @Override
    protected Map<String, Object> updateFakeData() {
        currentData.put("label", faker.book().genre());
        return currentData;
    }

    @Override
    protected Map<String, Object> partialUpdateFakeData() {
        return Map.of("label", faker.team().name());
    }

    @Override
    protected String getUrl() {
        return "/admin/menus";
    }

    @Test
    @Order(15)
    public void getAllMenuAsTreeView() throws Exception {
        String url = getUrl() + "/tree?filter=";
        this.test(url, HttpMethod.GET, null)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Order(16)
    public void getMenuByCurrentUser() throws Exception {
        String url = getUrl() + "/me";
        this.test(url, HttpMethod.GET, null)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
