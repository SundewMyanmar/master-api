package com.sdm.report.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Report {
    private String title;
    private String name;
    private List<Parameter> parameters;

    public Report(String title, String name) {
        this.title = title;
        this.name = name;
    }

    public enum ParameterType {
        INT, STRING
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameter {
        private String title;
        private String name;
        private ParameterType type;

    }
}
