package com.sdm.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.config.properties.PathProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Log4j2
@Service
public class SettingManager<T> {
    @Autowired
    PathProperties pathProperties;

    @Autowired
    ObjectMapper objectMapper;

    private boolean createSettingDirectory() {
        File file = new File(pathProperties.getSetting());
        if (Files.notExists(file.toPath()))
            return file.mkdirs();
        return true;
    }

    private File createSettingFile(String settingPath) throws IOException {
        File file = new File(pathProperties.getSetting() + settingPath);
        boolean booleanResult = true;
        if (!file.exists()) booleanResult = file.createNewFile();
        return booleanResult ? file : null;
    }

    public T loadSetting(String settingPath, Class<T> tClass) throws IOException {
        if (this.createSettingDirectory()) {
            File settingFile = this.createSettingFile(settingPath);
            if (settingFile != null) {
                InputStream in = new FileInputStream(settingFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String resultString = "", output = "";
                while ((output = reader.readLine()) != null) {
                    resultString += output;
                }
                reader.close();

                if (Globalizer.isNullOrEmpty(resultString)) resultString = "{}";

                return objectMapper.readValue(resultString, tClass);
            }
        }
        return null;
    }

    public boolean writeSetting(String settingPath, T setting) throws IOException {
        if (this.createSettingDirectory()) {
            File settingFile = this.createSettingFile(settingPath);
            if (!Globalizer.isNullOrEmpty(settingFile)) {
                String settingString = objectMapper.writeValueAsString(setting);
                Files.writeString(settingFile.toPath(), settingString, StandardCharsets.UTF_8);
                return true;
            }
        }

        return false;
    }
}
