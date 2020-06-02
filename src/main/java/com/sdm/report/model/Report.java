package com.sdm.report.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.birt.report.engine.api.IReportRunnable;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Report {
    private String id;

    private String name;

    private String storagePath;

    @JsonIgnore
    private IReportRunnable runnable;

    private List<Parameter> parameters;
}
