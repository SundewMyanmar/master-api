package com.sdm.reporting.controller;

import com.sdm.admin.model.Role;
import com.sdm.admin.repository.RoleRepository;
import com.sdm.core.controller.DefaultReadController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.util.Globalizer;
import com.sdm.reporting.model.Report;
import com.sdm.reporting.repository.ReportRepository;
import com.sdm.reporting.service.JasperReportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;


@Controller
@RequestMapping("/reports")
public class ReportController extends DefaultReadController<Report, String> {

    @Autowired
    private JasperReportService reportService;

    @Autowired
    private ReportRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    protected DefaultRepository<Report, String> getRepository() {
        return this.repository;
    }

    /**
     * Check Report Permission by Current User Roles
     *
     * @param reportId
     */
    private void checkPermission(String reportId) {
        Report report = checkData(reportId);
        if (report.isPublic()) {
            return;
        }

        Set<Integer> roles = report.getRoles().stream().map(Role::getId).collect(Collectors.toSet());
        //Check Roles
        List<Role> allowRoles = roleRepository.findRoleByUserIdAndRoleIds(getCurrentUser().getUserId(), roles)
                .orElseThrow(() -> new GeneralException(HttpStatus.FORBIDDEN,
                        localeManager.getMessage("access-denied")));

        if (allowRoles.size() <= 0) {
            throw new GeneralException(HttpStatus.FORBIDDEN, localeManager.getMessage("access-denied"));
        }
    }

    private void buildRoles(Report requestReport){
        if(!requestReport.isPublic()){
            List<Role> roles = roleRepository.findByIdIn(requestReport.getRoleIds());
            requestReport.setRoles(roles);
        }else{
            requestReport.setRoles(List.of());
        }
    }

    @GetMapping(value = "/view/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<?> viewReport(@RequestParam Map<String, Object> parameters,
                                        @PathVariable("id") String reportId) {
        checkPermission(reportId);
        String html = reportService.generateToHTML(reportId, parameters);
        return ResponseEntity.ok(html);
    }

    @GetMapping(value = "/print/{id}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> printReport(@RequestParam Map<String, Object> parameters,
                                         @PathVariable("id") String reportId) {
        checkPermission(reportId);
        byte[] data =  reportService.generateToPDF(reportId, parameters);
        Resource resource = new ByteArrayResource(data);
        String attachment = "attachment; filename=\"" + reportId + ".pdf\"";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, attachment)
                .body(resource);
    }

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Report> createReport(@Valid Report report,
                                               @RequestParam("reportFile") MultipartFile reportFile) {
        this.buildRoles(report);
        reportService.uploadReport(reportFile, report);
        return new ResponseEntity<>(report, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Report> updateReport(@Valid Report report,
                                               @PathVariable("id") String id,
                                               @RequestParam(value = "reportFile", required = false) MultipartFile reportFile) {
        this.checkData(id);
        this.buildRoles(report);

        if (!id.equals(report.getId())) {
            throw new GeneralException(HttpStatus.CONFLICT,
                    localeManager.getMessage("not-match-path-body-id"));
        }
        if (Globalizer.isNullOrEmpty(reportFile)) {
            repository.save(report);
        } else {
            reportService.uploadReport(reportFile, report);
        }
        return ResponseEntity.ok(report);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> remove(String id) {
        Report report = this.checkData(id);
        repository.softDelete(report);
        MessageResponse message = new MessageResponse(localeManager.getMessage("remove-success"),
                localeManager.getMessage("remove-data-by", id));
        return ResponseEntity.ok(message);
    }
}