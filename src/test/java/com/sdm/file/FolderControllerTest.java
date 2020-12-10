package com.sdm.file;

import com.sdm.core.DefaultReadWriteTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.Map;

public class FolderControllerTest extends DefaultReadWriteTest {
    @Override
    protected String getUrl() {
        return "/folders";
    }

    @Override
    protected Map<String, Object> createFakeData() {
        Map<String, Object> folder = new HashMap<>();
        folder.put("name", faker.book().genre());
        folder.put("color", faker.color().hex());
        folder.put("icon", faker.lorem().word());
        folder.put("priority", faker.number().numberBetween(0, 10));
        return folder;
    }

    @Override
    protected Map<String, Object> updateFakeData() {
        currentData.put("color", faker.color().hex());
        return currentData;
    }

    @Override
    protected Map<String, Object> partialUpdateFakeData() {
        return Map.of("color", faker.book().genre());
    }

    @Test
    @Order(20)
    public void getAllMenuAsTreeView() throws Exception {
        String url = getUrl() + "/root?filter=";
        this.test(url, HttpMethod.GET, null)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
