package com.sdm.report.controller;

import com.sdm.report.request.OutputType;
import com.sdm.report.request.Report;
import com.sdm.report.service.ReportService;
import lombok.extern.log4j.Log4j2;
import org.eclipse.birt.report.engine.api.EngineException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller("/reports")
@Log4j2
public class ReportController {

    @Autowired
    private ReportService reportService;

    @RequestMapping(produces = "application/json", method = RequestMethod.GET, value = "")
    @ResponseBody
    public List<Report> listReports() {
        return reportService.getReports();
    }

    @RequestMapping(produces = "application/json", method = RequestMethod.GET, value = "/reload")
    @ResponseBody
    public ResponseEntity reloadReports(HttpServletResponse response) {
        try {
            log.info("Reloading reports");
            reportService.loadReports();
        } catch (EngineException e) {
            log.error("There was an error reloading the reports in memory: ", e);
            return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{name}")
    @ResponseBody
    public void generateFullReport(HttpServletResponse response, HttpServletRequest request,
                                   @PathVariable("name") String name, @RequestParam("output") String output) {
        log.info("Generating full report: " + name + "; format: " + output);
        OutputType format = OutputType.from(output);
        reportService.generateMainReport(name, format, response, request);
    }
}
