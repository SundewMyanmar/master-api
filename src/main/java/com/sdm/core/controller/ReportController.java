package com.sdm.core.controller;

import com.sdm.core.model.response.ListResponse;
import com.sdm.core.service.JasperReportService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private JasperReportService jasperReportService;

    @GetMapping("")
    public ResponseEntity<ListResponse<String>> listReports() {
        List<String> reports = new ArrayList<>(jasperReportService.getReports().keySet());
        return ResponseEntity.ok(new ListResponse<>(reports));
    }

    @GetMapping("/build")
    public ResponseEntity<ListResponse<String>> buildReports(HttpServletResponse response) {
        log.info("Reloading reports");
        jasperReportService.compileReports();
        List<String> reports = new ArrayList<>(jasperReportService.getReports().keySet());
        return ResponseEntity.ok(new ListResponse<>(reports));
    }

    @GetMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String renderHTML(HttpServletRequest request, @PathVariable("id") String id) {
        Map<String, Object> parameters = new HashMap<>();
        var requestParams = request.getParameterNames();
        while (requestParams.hasMoreElements()) {
            String key = requestParams.nextElement();
            parameters.put(key, request.getParameter(key));
        }
        return jasperReportService.generateToHTML(id, parameters);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity generatePDF(HttpServletRequest request, @PathVariable("id") String id) {
        Map<String, Object> parameters = new HashMap<>();
        var requestParams = request.getParameterNames();
        while (requestParams.hasMoreElements()) {
            String key = requestParams.nextElement();
            parameters.put(key, request.getParameter(key));
        }
        return jasperReportService.generateToPDF(id, parameters);
    }
}
