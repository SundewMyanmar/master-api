package com.sdm.inventory;

import com.sdm.core.DefaultReadWriteTest;

import java.util.Map;

public class AttributeControllerTest extends DefaultReadWriteTest {
    private final String[] AVAILABLE_TYPES = {"TEXT", "INTEGER", "FLOAT", "DATE", "DATETIME", "CHOICE", "LIST", "YES_NO"};

    @Override
    protected String getUrl() {
        return "/inventory/attributes";
    }

    @Override
    protected Map<String, Object> createFakeData() {
        return Map.of("name", faker.book().title(),
                "description", faker.lorem().paragraph(1),
                "guild", faker.book().genre(),
                "type", AVAILABLE_TYPES[faker.number().numberBetween(0, AVAILABLE_TYPES.length - 1)],
                "usedUom", faker.random().nextBoolean());
    }

    @Override
    protected Map<String, Object> updateFakeData() {
        currentData.put("description", "Modified auto generated text.");
        return currentData;
    }

    @Override
    protected Map<String, Object> partialUpdateFakeData() {
        return Map.of("name", faker.book().title(),
                "guild", faker.book().genre());
    }
}
