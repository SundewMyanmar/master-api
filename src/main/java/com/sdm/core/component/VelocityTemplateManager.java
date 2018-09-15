/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.component;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author htoonlin
 */
@Component
public class VelocityTemplateManager {

    private static final Logger LOG = Logger.getLogger(VelocityTemplateManager.class.getName());

    private final VelocityEngine engine;

    @Value("${com.sdm.template.path}")
    private String path = "./template";

    public VelocityTemplateManager() {
        this.engine = new VelocityEngine();
        this.engine.addProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, this.path);
        this.engine.init();
    }


    public String buildTemplate(String template, Map<String, Object> data) {
        VelocityContext context = new VelocityContext(data);
        Template vm = engine.getTemplate(template);
        StringWriter writer = new StringWriter();
        vm.merge(context, writer);
        return writer.toString();
    }

}
