package com.sdm.admin;

import com.sdm.core.DefaultReadWriteTest;

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
}
