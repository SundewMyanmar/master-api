package com.sdm.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.Constants;
import com.sdm.core.config.properties.PathProperties;
import com.sdm.core.controller.DefaultController;
import com.sdm.core.exception.GeneralException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/admin/config")
public class SystemConfigController extends DefaultController {
    @Autowired
    private PathProperties pathProperties;

    @Autowired
    private ObjectMapper mapper;

    private Path getConfigFile() {
        return Paths.get(pathProperties.getSetting(), Constants.SYSTEM_CONFIG);
    }

    @GetMapping("")
    public ResponseEntity<String> getSystemConfig() {
        try {
            String config = Files.readString(getConfigFile());
            return ResponseEntity.ok(config);
        } catch (IOException e) {
            throw new GeneralException(HttpStatus.NO_CONTENT, localeManager.getMessage("no-data"));
        }
    }

    @PostMapping("")
    public ResponseEntity<Map<String, Object>> saveConfig(@RequestBody Map<String, Object> data) {
        try {
            mapper.writeValue(getConfigFile().toFile(), data);
            return new ResponseEntity<Map<String, Object>>(data, HttpStatus.CREATED);
        } catch (IOException e) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
        }
    }
}
