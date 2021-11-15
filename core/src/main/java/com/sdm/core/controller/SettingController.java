package com.sdm.core.controller;

import com.sdm.core.exception.GeneralException;
import com.sdm.core.util.SettingManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/system/config")
public class SettingController extends DefaultController {
    @Autowired
    private SettingManager settingManager;

    @GetMapping("")
    public ResponseEntity<String> getSystemConfig(@RequestParam("fileName") String fileName) {
        try {
            String config = settingManager.loadSetting(fileName);
            return ResponseEntity.ok(config);
        } catch (IOException e) {
            throw new GeneralException(HttpStatus.NO_CONTENT, localeManager.getMessage("no-data"));
        }
    }

    @PostMapping("")
    public ResponseEntity<Map<String, Object>> saveConfig(@RequestBody Map<String, Object> data,
                                                          @RequestParam("fileName") String fileName) {
        try {
            settingManager.writeSetting(fileName, data);
            return new ResponseEntity<>(data, HttpStatus.CREATED);
        } catch (IOException e) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
        }
    }
}
