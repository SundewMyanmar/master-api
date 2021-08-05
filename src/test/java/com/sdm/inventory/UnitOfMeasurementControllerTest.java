package com.sdm.inventory;

import com.sdm.core.DefaultReadWriteTest;

import java.util.Map;

public class UnitOfMeasurementControllerTest extends DefaultReadWriteTest {
    @Override
    protected String getUrl() {
        return "/inventory/uoms";
    }

    @Override
    protected Map<String, Object> createFakeData() {
        return Map.of("code", faker.country().countryCode3(),
                "name", faker.country().name(),
                "guild", faker.country().capital(),
                "relatedUom", faker.number().numberBetween(1, 10),
                "relatedValue", faker.number().randomDouble(10, 0, 10)
        );
    }

    @Override
    protected Map<String, Object> updateFakeData() {
        currentData.put("name", faker.country().name());
        return currentData;
    }

    @Override
    protected Map<String, Object> partialUpdateFakeData() {
        return Map.of("relatedUom", faker.number().numberBetween(1, 10),
                "relatedValue", faker.number().randomDouble(10, 0, 10));
    }
}
