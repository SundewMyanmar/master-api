package com.sdm.inventory;

import com.sdm.core.DefaultReadWriteTest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ProductControllerTest extends DefaultReadWriteTest {
    @Override
    protected String getUrl() {
        return "/inventory/products";
    }

    public Map<String, Object> createProductAttribute(int productId) {
        Map<String, Object> data = new HashMap<>();
        if (productId > 0) {
            data.put("productId", productId);
        }
        data.put("attribute", Map.of("id", 1));
        data.put("value", faker.book().genre());
        data.put("uom", Map.of("id", 1));
        return data;
    }

    @Override
    protected Map<String, Object> createFakeData() {
        Map<String, Object> data = new HashMap<>();
        data.put("code", faker.code().ean13());
        data.put("barCode", faker.code().ean13());
        data.put("barcodeType", "EAN_13");
        data.put("name", faker.commerce().productName());
        data.put("shortDescription", faker.lorem().paragraph(2));
        data.put("description", faker.lorem().paragraph(50));
        data.put("tags", faker.lorem().words(3));
        data.put("currentBal", faker.number().numberBetween(100, 1000));
        data.put("minBal", faker.number().numberBetween(1, 10));
        data.put("maxBal", faker.number().numberBetween(300, 1000));
        data.put("reorderBal", faker.number().numberBetween(1, 20));
        data.put("status", "AVAILABLE");
        data.put("activeAt", faker.date().future(7, TimeUnit.DAYS));
        return data;
    }

    @Override
    protected Map<String, Object> updateFakeData() {
        currentData.put("name", faker.commerce().productName());
        return currentData;
    }

    @Override
    protected Map<String, Object> partialUpdateFakeData() {
        return Map.of("shortDescription", faker.lorem().paragraph(3),
                "description", faker.lorem().paragraph(50));
    }
}
