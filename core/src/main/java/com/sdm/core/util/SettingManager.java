package com.sdm.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


@Service
@Log4j2
public class SettingManager {

    @Value("${com.sdm.path.setting}")
    private String settingRootPath = "/var/www/master-api/setting/";

    @Autowired
    ObjectMapper objectMapper;

    @PostConstruct
    private void init() {
        Path rootPath = Path.of(settingRootPath);
        if (Files.notExists(rootPath)) {
            try {
                Files.createDirectories(rootPath);
            } catch (IOException ex) {
                log.warn(ex.getLocalizedMessage());
            }
        }
    }

    private File createSettingFile(String settingPath) throws IOException {
        File file = new File(settingRootPath + settingPath);
        boolean booleanResult = true;
        if (!file.exists()) booleanResult = file.createNewFile();
        return booleanResult ? file : null;
    }

    public String loadSetting(String setting) throws IOException {
        return Files.readString(Path.of(settingRootPath, setting));
    }

    public <T> T loadSetting(String setting, Class<T> refClass) throws IOException {
        String resultString = this.loadSetting(setting);
        if (Globalizer.isNullOrEmpty(resultString))
            resultString = "{}";

        return objectMapper.readValue(resultString, refClass);
    }

    public void writeSetting(String settingPath, Object setting) throws IOException {
        File settingFile = this.createSettingFile(settingPath);
        if (!Globalizer.isNullOrEmpty(settingFile)) {
            String settingString = objectMapper.writeValueAsString(setting);
            Files.writeString(settingFile.toPath(), settingString, StandardCharsets.UTF_8);
        }
    }
}
