package com.sdm.inventory;

import com.sdm.core.DefaultReadWriteTest;

import java.util.HashMap;
import java.util.Map;

public class CategoryControllerTest extends DefaultReadWriteTest {
    @Override
    protected String getUrl() {
        return "/inventory/categories";
    }

    @Override
    protected Map<String, Object> createFakeData() {
        Map<String, Object> fakeData = new HashMap<>();
        fakeData.put("name", faker.book().genre());
        fakeData.put("description", faker.lorem().paragraph(1));
        return fakeData;
    }

    @Override
    protected Map<String, Object> updateFakeData() {
        currentData.put("description", "Modified automatic generated category.");
        return currentData;
    }

    @Override
    protected Map<String, Object> partialUpdateFakeData() {
        return Map.of("name", faker.book().genre());
    }
}
