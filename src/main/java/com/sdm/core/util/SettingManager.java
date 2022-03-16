package com.sdm.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.util.annotation.SettingFile;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


@Service
@Log4j2
public class SettingManager {
    @Value("${com.sdm.path.setting:/var/www/master-api/setting/}")
    private String settingRootPath;

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

    private File createSettingFile(String setting) throws IOException {
        File file = new File(settingRootPath + setting);
        boolean booleanResult = true;
        if (!file.exists()) booleanResult = file.createNewFile();
        return booleanResult ? file : null;
    }

    public String loadSetting(Path settingPath) throws IOException {
        return Files.readString(settingPath);
    }

    public String loadSetting(String setting) throws IOException {
        return this.loadSetting(Path.of(settingRootPath, setting));
    }

    public <T> T loadSetting(String setting, Class<T> refClass) throws IOException {
        Path settingPath = Path.of(settingRootPath, setting);
        if(!Files.exists(settingPath)){
            try {
                T data = refClass.getDeclaredConstructor().newInstance();
                this.writeSetting(setting, data);
            }catch(IOException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex){
                log.warn(ex.getLocalizedMessage());
            }
        }
        String resultString = this.loadSetting(settingPath);
        if (Globalizer.isNullOrEmpty(resultString))
            resultString = "{}";

        return objectMapper.readValue(resultString, refClass);
    }

    public <T> T loadSetting(Class<T> refClass) throws IOException {
        if (!refClass.isAnnotationPresent(SettingFile.class)) {
            throw new RuntimeException("Can't find setting info.");
        }
        SettingFile settingFile = refClass.getAnnotation(SettingFile.class);
        return this.loadSetting(settingFile.value(), refClass);
    }

    public void writeSetting(String setting, Object data) throws IOException {
        File settingFile = this.createSettingFile(setting);
        if (!Globalizer.isNullOrEmpty(settingFile)) {
            String settingString = objectMapper.writeValueAsString(data);
            Files.writeString(settingFile.toPath(), settingString, StandardCharsets.UTF_8);
        }
    }

    public <T> void writeSetting(T setting, Class<T> refClass) throws IOException {
        if (!refClass.isAnnotationPresent(SettingFile.class)) {
            throw new RuntimeException("Can't find setting info.");
        }
        SettingFile settingFile = refClass.getAnnotation(SettingFile.class);
        this.writeSetting(settingFile.value(), setting);
    }
}
