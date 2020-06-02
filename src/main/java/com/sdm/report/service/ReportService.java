package com.sdm.report.service;

import com.sdm.core.config.properties.PathProperties;
import com.sdm.report.model.Parameter;
import com.sdm.report.model.Report;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.log4j.Log4j2;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.*;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.OdaDataSourceHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.core.runtime.Path;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;
import java.util.logging.Level;

@Log4j2
@Service
public class ReportService implements DisposableBean {

    private static final String REPORT_SUFFIX = ".rptdesign";
    @Autowired
    private ApplicationContext context;

    @Autowired
    private PathProperties pathProperties;
    @Autowired
    private HikariDataSource dataSource;
    private Map<String, Report> reports;

    private IReportEngine birtEngine;

    @SuppressWarnings("unchecked")
    @PostConstruct
    protected void init() throws BirtException {
        EngineConfig engineConfig = new EngineConfig();
        engineConfig.setEngineHome(pathProperties.getReport() + Path.SEPARATOR + "engine");
        engineConfig.setLogConfig(pathProperties.getReportLog(), Level.WARNING);
        engineConfig.getAppContext().put("spring", this.context);
        Platform.startup();

        IReportEngineFactory reportEngineFactory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
        birtEngine = reportEngineFactory.createReportEngine(engineConfig);
        log.info("Created birt report engine!");
        loadReports();
    }

    private Parameter createParameter(IGetParameterDefinitionTask parameterDefinitionTask, IScalarParameterDefn scalar) {
        Parameter parameter = new Parameter(scalar.getName(), scalar.getScalarParameterType());
        if (!StringUtils.isEmpty(scalar.getPromptText())) {
            parameter.setTitle(scalar.getPromptText());
        }
        //Parameter is a List Box
        if (scalar.getControlType() == IScalarParameterDefn.LIST_BOX) {
            Collection selectionList = parameterDefinitionTask.getSelectionList(scalar.getName());
            //Selection contains data
            if (selectionList != null) {
                for (Iterator sliter = selectionList.iterator(); sliter.hasNext(); ) {
                    //Print out the selection choices
                    IParameterSelectionChoice selectionItem = (IParameterSelectionChoice) sliter.next();
                    String value = (String) selectionItem.getValue();
                    String label = selectionItem.getLabel();
                    parameter.getAvailableValues().put(label, value);
                }
            }
        }
        return parameter;
    }

    private List<Parameter> getParameters(IReportRunnable runnable) {
        List<Parameter> parameters = new ArrayList<>();
        IGetParameterDefinitionTask parameterDefinitionTask = birtEngine.createGetParameterDefinitionTask(runnable);
        Collection params = parameterDefinitionTask.getParameterDefns(true);
        Iterator iter = params.iterator();
        //Iterate over all parameters
        while (iter.hasNext()) {
            IParameterDefnBase param = (IParameterDefnBase) iter.next();
            //Group section found
            if (param instanceof IParameterGroupDefn) {
                //Get Group Name
                IParameterGroupDefn group = (IParameterGroupDefn) param;
                //Get the parameters within a group
                Iterator i2 = group.getContents().iterator();
                while (i2.hasNext()) {
                    IScalarParameterDefn scalar = (IScalarParameterDefn) i2.next();
                    Parameter parameter = createParameter(parameterDefinitionTask, scalar);
                    parameter.setGroup(group.getName());
                    parameters.add(parameter);
                }
            } else {
                //Parameters are not in a group
                IScalarParameterDefn scalar = (IScalarParameterDefn) param;
                Parameter parameter = createParameter(parameterDefinitionTask, scalar);
                parameters.add(parameter);
            }
        }
        parameterDefinitionTask.close();
        return parameters;
    }

    private void loadDataSource(DesignElementHandle designElementHandle) throws SemanticException {
        for (Object handle : designElementHandle.getModuleHandle().getAllDataSources()) {
            if (handle instanceof OdaDataSourceHandle) {
                OdaDataSourceHandle dataSourceHandle = (OdaDataSourceHandle) handle;
                dataSourceHandle.setProperty(OdaProperty.DRIVER, dataSource.getDriverClassName());
                dataSourceHandle.setProperty(OdaProperty.URL, dataSource.getJdbcUrl());
                dataSourceHandle.setProperty(OdaProperty.USER, dataSource.getUsername());
                dataSourceHandle.setProperty(OdaProperty.PASSWORD, dataSource.getPassword());
                log.info(String.format("Changed Data Source.", reports.size()));
            }
        }
    }

    /**
     * Load report files to memory
     */
    public void loadReports() throws EngineException {
        reports = new HashMap<>();
        File rootFolder = new File(pathProperties.getReport());
        for (String file : Objects.requireNonNull(rootFolder.list())) {
            if (!file.endsWith(REPORT_SUFFIX)) {
                continue;
            }

            String fullPath = rootFolder.getAbsolutePath() + File.separator + file;
            Report report = new Report();
            report.setId(UUID.randomUUID().toString());
            report.setStoragePath(fullPath);
            IReportRunnable runnable = birtEngine.openReportDesign(fullPath);
            try {
                this.loadDataSource(runnable.getDesignHandle());
            } catch (SemanticException ex) {
                log.warn(ex.getLocalizedMessage());
            }
            report.setRunnable(runnable);
            report.setName(runnable.getDesignHandle().getDisplayLabel());
            report.setParameters(getParameters(runnable));
            reports.put(report.getId(), report);
        }
        log.info(String.format("Loaded %d birt reports.", reports.size()));
    }

    public List<Report> getReports() {
        return new ArrayList<>(reports.values());
    }

    public void generatePDF(String reportId, HttpServletRequest request, HttpServletResponse response) {
        Report report = reports.get(reportId);
        IRunAndRenderTask runAndRenderTask = birtEngine.createRunAndRenderTask(report.getRunnable());
        runAndRenderTask.getAppContext().put(EngineConstants.APPCONTEXT_PDF_RENDER_CONTEXT, request);

        var requestParams = request.getParameterNames();
        while (requestParams.hasMoreElements()) {
            String key = requestParams.nextElement();
            runAndRenderTask.setParameterValue(key, request.getParameter(key));
        }
        runAndRenderTask.validateParameters();

        response.setContentType(birtEngine.getMIMEType("pdf"));
        IRenderOption options = new RenderOption();
        PDFRenderOption pdfRenderOption = new PDFRenderOption(options);
        pdfRenderOption.setOutputFormat("pdf");
        runAndRenderTask.setRenderOption(pdfRenderOption);

        try {
            pdfRenderOption.setOutputStream(response.getOutputStream());
            runAndRenderTask.run();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            runAndRenderTask.close();
        }
    }

    @Override
    public void destroy() throws Exception {
        birtEngine.destroy();
        Platform.shutdown();
    }

    protected interface OdaProperty {
        String DRIVER = "odaDriverClass";
        String URL = "odaURL";
        String USER = "odaUser";
        String PASSWORD = "odaPassword";
    }
}
