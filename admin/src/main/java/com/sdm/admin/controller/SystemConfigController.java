package com.sdm.admin.controller;

import com.sdm.core.Constants;
import com.sdm.core.controller.DefaultController;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.util.SettingManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/admin/config")
public class SystemConfigController extends DefaultController {
    @Autowired
    private SettingManager settingManager;

    @GetMapping("")
    public ResponseEntity<String> getSystemConfig() {
        try {
            String config = settingManager.loadSetting(Constants.SYSTEM_CONFIG);
            return ResponseEntity.ok(config);
        } catch (IOException e) {
            throw new GeneralException(HttpStatus.NO_CONTENT, localeManager.getMessage("no-data"));
        }
    }

    @PostMapping("")
    public ResponseEntity<Map<String, Object>> saveConfig(@RequestBody Map<String, Object> data) {
        try {
            settingManager.writeSetting(Constants.SYSTEM_CONFIG, data);
            return new ResponseEntity<>(data, HttpStatus.CREATED);
        } catch (IOException e) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
        }
    }
}
