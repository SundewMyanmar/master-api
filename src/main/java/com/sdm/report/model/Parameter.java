package com.sdm.report.model;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Parameter {
    private String title;

    @NonNull
    private String name;
    @NonNull
    private String type;

    private String group;
    private Map<String, String> availableValues = new HashMap<>();
}
