package com.sdm.reporting.service;

import com.sdm.core.Constants;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.LocaleManager;
import com.sdm.reporting.model.Report;
import com.sdm.reporting.repository.ReportRepository;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.HtmlResourceHandler;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
/*
  https://community.jaspersoft.com/documentation/jasperreports-server-user-guide/exporting-report
 */
public class JasperReportService {

    protected interface Suffix {
        String DESIGN = ".jrxml";
        String COMPILE = ".jasper";
    }

    @Value("${com.sdm.path.report:/var/www/master-api/report/}")
    private String reportRootPath;

    @Autowired
    private LocaleManager localeManager;

    @Autowired
    private ReportRepository repository;

    @Autowired
    private DataSource dataSource;

    public static class Base64ResourceHandler implements HtmlResourceHandler {
        final Map<String, String> images;

        public Base64ResourceHandler() {
            this.images = new HashMap<>();
        }

        @Override
        public String getResourcePath(String id) {
            return images.get(id);
        }

        @Override
        public void handleResource(String id, byte[] data) {
            images.put(id, "data:image/png;base64," + new String(Base64.getEncoder().encode(data)));
        }
    }


    public Report checkReport(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("no-data-by", id)));
    }

    private Path getReportFile(String id, String type) {
        return Paths.get(reportRootPath, id + type).normalize();
    }

    @Transactional
    public void uploadReport(MultipartFile reportFile, Report report) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(reportFile.getOriginalFilename()));

        //Check file extension
        String[] fileInfo = fileName.split("\\.(?=[^\\.]+$)");
        String ext = "";
        if (fileInfo.length > 1) {
            ext = "." + fileInfo[fileInfo.length - 1].toLowerCase(Locale.ROOT);
        }
        if (Globalizer.isNullOrEmpty(ext) || !List.of(Suffix.DESIGN, Suffix.COMPILE).contains(ext)) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-report-extension", fileName));
        }
        //Update or create report model
        if (!Globalizer.isNullOrEmpty(report.getId())) {
            Optional<Report> existReport = repository.findById(report.getId());
            if (existReport.isEmpty()) {
                report.setId(UUID.randomUUID().toString());
            }
        } else {
            report.setId(UUID.randomUUID().toString());
        }

        try {
            //Store report
            //If design, compile report
            if (ext.equalsIgnoreCase(Suffix.DESIGN)) {
                Path designFile = getReportFile(report.getId(), Suffix.DESIGN);
                reportFile.transferTo(designFile);
                compileReport(report.getId());
                report.setHasDesign(true);
            } else {
                reportFile.transferTo(getReportFile(report.getId(), Suffix.COMPILE));
            }
            repository.save(report);
        } catch (IOException ex) {
            log.warn(ex.getLocalizedMessage());
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    public void compileReport(String id) {
        Path designFile = getReportFile(id, Suffix.DESIGN);
        Path compileFile = getReportFile(id, Suffix.COMPILE);
        if (Files.notExists(designFile)) {
            log.warn("Invalid Report File Path");
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("report-compiled-failed"));
        }

        log.info("Compiling report : " + id);
        try (FileInputStream inputStream = new FileInputStream(designFile.toFile())) {
            JasperReport report = JasperCompileManager.compileReport(inputStream);
            JRSaver.saveObject(report, compileFile.toFile());
            log.info("Compiled report " + report.getName());
        } catch (IOException | JRException ex) {
            log.warn(ex.getLocalizedMessage());
        }
    }

    public JasperPrint loadReport(String reportId, Map<String, Object> parameters) {
        Report report = checkReport(reportId);
        Path reportFile = getReportFile(reportId, Suffix.COMPILE);
        log.info(String.format("Load Report %s => %s", report.getId(), report.getName()));

        //Load report
        try (FileInputStream inputStream = new FileInputStream(reportFile.toFile())) {
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(inputStream);
            try (Connection connection = dataSource.getConnection()) {
                return JasperFillManager.fillReport(jasperReport, parameters, connection);
            } catch (SQLException ex) {
                log.warn(ex.getLocalizedMessage());
            }
        } catch (IOException | JRException ex) {
            log.warn(ex.getLocalizedMessage());
        }
        throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load report.");
    }

    public String generateToHTML(String reportId, Map<String, Object> parameters) {
        JasperPrint print = loadReport(reportId, parameters);
        HtmlExporter exporter = new HtmlExporter();
        exporter.setExporterInput(new SimpleExporterInput(print));

        StringBuilder html = new StringBuilder();
        SimpleHtmlExporterOutput output = new SimpleHtmlExporterOutput(html);

        output.setImageHandler(new Base64ResourceHandler());

        exporter.setExporterOutput(output);
        try {
            exporter.exportReport();
        } catch (JRException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }

        return html.toString();
    }

    public ResponseEntity<?> generateToPDF(String reportId, Map<String, Object> parameters) {
        JasperPrint print = loadReport(reportId, parameters);
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(print));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));
        SimplePdfReportConfiguration reportConfig = new SimplePdfReportConfiguration();
        reportConfig.setSizePageToContent(true);
        reportConfig.setForceLineBreakPolicy(false);

        SimplePdfExporterConfiguration exportConfig = new SimplePdfExporterConfiguration();
        exportConfig.setMetadataAuthor(Constants.INFO_MAIL);
        exportConfig.setAllowedPermissionsHint("PRINTING");

        exporter.setConfiguration(reportConfig);
        exporter.setConfiguration(exportConfig);

        try {
            exporter.exportReport();
        } catch (JRException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }

        byte[] data = output.toByteArray();
        Resource resource = new ByteArrayResource(data);
        String attachment = "attachment; filename=\"" + reportId + ".pdf\"";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, attachment)
                .body(resource);
    }
}
