package com.sdm.core.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

public interface ISettingManager {
    @PostConstruct
    void init();

    List<Map<String, Object>> buildStructure(Class<?> cls, Object data) throws IllegalAccessException;

    List<Map<String, Object>> getAllSettings() throws ClassNotFoundException, IllegalAccessException;

    String loadSetting(Path settingPath) throws IOException;

    String loadSetting(String className) throws IOException, ClassNotFoundException;

    <T> T loadSetting(String setting, Class<T> refClass) throws IOException, IllegalAccessException;

    <T> T loadSetting(Class<T> refClass) throws IOException, IllegalAccessException;

    Object writeSetting(String className, Object data) throws IOException, ClassNotFoundException, IllegalAccessException;

    <T> void writeSetting(T setting, Class<T> refClass) throws IOException, IllegalAccessException;
}
