package com.sdm.core.controller;

import com.sdm.core.exception.GeneralException;
import com.sdm.core.util.SettingManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/system/config")
public class SettingController extends DefaultController {
    @Autowired
    private SettingManager settingManager;

    @GetMapping("/struct")
    public ResponseEntity<List<Map<String,Object>>> getSettingStructure() throws ClassNotFoundException {
        var result=settingManager.getSettingStructure();
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
    public ResponseEntity<Map<String, Object>> saveConfig(@RequestBody Map<String, Object> data,
                                                          @RequestParam("className") String className) {
        try {
            settingManager.writeSetting(className, data);
            return new ResponseEntity<>(data, HttpStatus.CREATED);
        } catch (IOException | ClassNotFoundException | IllegalAccessException e) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
        }
    }
}
