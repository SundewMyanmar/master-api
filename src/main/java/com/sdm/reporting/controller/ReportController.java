package com.sdm.reporting.controller;

import com.sdm.core.controller.DefaultReadController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.util.Globalizer;
import com.sdm.reporting.model.Report;
import com.sdm.reporting.repository.ReportRepository;
import com.sdm.reporting.service.JasperReportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.Map;

import javax.validation.Valid;


@RequestMapping("/reports")
@Controller
public class ReportController extends DefaultReadController<Report, String> {

    @Autowired
    private JasperReportService reportService;

    @Autowired
    private ReportRepository repository;

    @Override
    protected DefaultRepository<Report, String> getRepository() {
        return this.repository;
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<?> viewReport(@RequestParam Map<String, Object> parameters,
                                        @PathVariable("id") String reportId){
        String html = reportService.generateToHTML(reportId, parameters);
        return ResponseEntity.ok(html);
    }

    @GetMapping("/print/{id}")
    public ResponseEntity<?> printReport(@RequestParam Map<String, Object> parameters,
                                        @PathVariable("id") String reportId){
        return reportService.generateToPDF(reportId, parameters);
    }

    @Transactional
    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Report> createReport(@Valid Report report,
                                                         @RequestParam("reportFile") MultipartFile reportFile) {
        reportService.uploadReport(reportFile, report);
        return new ResponseEntity<>(report, HttpStatus.CREATED);
    }

    @Transactional
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Report> updateReport(@Valid Report report,
                                               @PathVariable("id") String id,
                                               @RequestParam(value = "reportFile", required = false) MultipartFile reportFile) {
        this.checkData(id);
        if (!id.equals(report.getId())) {
            throw new GeneralException(HttpStatus.CONFLICT,
                    localeManager.getMessage("not-match-path-body-id"));
        }
        if(Globalizer.isNullOrEmpty(reportFile)){
            repository.save(report);
        }else{
            reportService.uploadReport(reportFile, report);
        }
        return ResponseEntity.ok(report);
    }

    @Transactional
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> remove(String id) {
        Report report = this.checkData(id);
        repository.softDelete(report);
        MessageResponse message = new MessageResponse(localeManager.getMessage("remove-success"),
                localeManager.getMessage("remove-data-by", id));
        return ResponseEntity.ok(message);
    }
}
