package com.sdm.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModelInfo {

    public enum Alignment {
        left,
        center,
        right
    }

    ;

    public enum GridType {
        text,
        image,
        icon,
        bool,
        raw
    }

    ;

    public class GridInfo {
        private Alignment alignment;
        private GridType type;
        private int minWidth;
        private boolean filterable;
        private boolean sortable;
    }

    private String name;
    private String dataType;
    private String type;
    private String label;
    private boolean primaryKey;
    private boolean required;
    private int min;
    private int max;
    private GridInfo grid;
    private Map<String, Object> extras;

    public void addExtra(String key, Object value) {
        if (extras == null) {
            extras = new HashMap<>();
        }
        extras.put(key, value);
    }
}
