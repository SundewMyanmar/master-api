package com.sdm.core.controller;

import com.sdm.core.exception.GeneralException;
import com.sdm.core.util.ISettingManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/config")
public class SettingController extends DefaultController {
    @Autowired
    private ISettingManager settingManager;

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getSettingStructure() throws ClassNotFoundException, IllegalAccessException {
        var result = settingManager.getAllSettings();
        return ResponseEntity.ok(result);
    }

    @GetMapping("")
    public ResponseEntity<String> getSystemConfig(@RequestParam("className") String className) {
        try {
            String config = settingManager.loadSetting(className);
            return ResponseEntity.ok(config);
        } catch (IOException | ClassNotFoundException e) {
            throw new GeneralException(HttpStatus.NO_CONTENT, localeManager.getMessage("no-data"));
        }
    }

    @PostMapping("")
    public ResponseEntity<?> saveConfig(@RequestBody Map<String, Object> data,
                                        @RequestParam("className") String className) {
        try {
            var result = settingManager.writeSetting(className, data);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (IOException | ClassNotFoundException | IllegalAccessException e) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
        }
    }
}
