package com.sdm.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModelInfo {

    private String name;
    private String dataType;
    private String type;
    private String label;
    private boolean primaryKey;
    private boolean required;
    private int min;
    private int max;
    private Map<String, Object> extras;

    public void addExtra(String key, Object value) {
        if (extras == null) {
            extras = new HashMap<>();
        }
        extras.put(key, value);
    }
}
