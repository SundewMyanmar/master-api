package com.sdm.core.service;

import com.sdm.Constants;
import com.sdm.core.config.properties.PathProperties;
import com.sdm.core.exception.GeneralException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.HtmlResourceHandler;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.export.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Log4j2
/**
 * https://community.jaspersoft.com/documentation/jasperreports-server-user-guide/exporting-report
 */
public class JasperReportService {

    @Autowired
    private PathProperties pathProperties;
    @Autowired
    private DataSource dataSource;
    @Getter
    private Map<String, String> reports;

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

    @PostConstruct
    protected void init() {
        compileReports();
    }

    public void compileReports() {
        log.info("Compiling reports...");
        File rootFolder = new File(pathProperties.getReport());
        if (!rootFolder.exists() || !rootFolder.isDirectory()) {
            log.warn("Invalid Report File Path");
            return;
        }

        this.reports = new HashMap<>();
        for (File file : Objects.requireNonNull(rootFolder.listFiles())) {
            if (!file.getName().endsWith(Suffix.DESIGN)) {
                continue;
            }
            try (FileInputStream inputStream = new FileInputStream(file)) {
                JasperReport designFile = JasperCompileManager.compileReport(inputStream);
                File jasperFile = new File(file.getAbsolutePath().replace(Suffix.DESIGN, Suffix.COMPILE));
                JRSaver.saveObject(designFile, jasperFile);

                log.info("Compiled report " + jasperFile.getName());
                this.reports.put(jasperFile.getName().replace(Suffix.COMPILE, ""), jasperFile.getAbsolutePath());
            } catch (IOException | JRException ex) {
                log.warn(ex.getLocalizedMessage());
            }
        }
    }

    public JasperPrint loadReport(String reportId, Map<String, Object> parameters) {
        File compileFile = new File(this.reports.get(reportId));
        log.info("Load Report " + compileFile.getName());
        try (FileInputStream inputStream = new FileInputStream(compileFile)) {
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(inputStream);
            return JasperFillManager.fillReport(jasperReport, parameters, dataSource.getConnection());
        } catch (IOException | JRException | SQLException ex) {
            log.warn(ex.getLocalizedMessage());
        }
        throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load report.");
    }

    public class Base64ResourceHandler implements HtmlResourceHandler {
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
        exportConfig.setEncrypted(true);
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

    protected interface Suffix {
        String DESIGN = ".jrxml";
        String COMPILE = ".jasper";
    }
}
