package com.sdm.admin;

import com.sdm.core.DefaultReadWriteTest;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class RoleControllerTest extends DefaultReadWriteTest {
    public static final int SKIP_ROLE_ID = 1;

    @Override
    public String getUrl() {
        return "/admin/roles";
    }

    @Override
    public Map<String, Object> createFakeData() {
        String name = faker.team().name();
        String description = faker.lorem().paragraph();
        return Map.of("name", name, "description", description);
    }

    @Override
    protected Map<String, Object> updateFakeData() {
        currentData.put("description", "Modified automatic generated role to test.");
        return currentData;
    }

    @Override
    protected Map<String, Object> partialUpdateFakeData() {
        return Map.of("name", faker.team().name());
    }

    @Override
    protected List<Serializable> skipRemoveIds() {
        return List.of(SKIP_ROLE_ID);
    }
}
