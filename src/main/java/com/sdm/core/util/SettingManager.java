package com.sdm.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.config.PropertyConfig;
import com.sdm.core.util.annotation.SettingFile;
import com.sdm.core.util.annotation.Encrypt;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


@Service
@Log4j2
public class SettingManager {
    @Value("${com.sdm.path.setting:/var/www/master-api/setting/}")
    private String settingRootPath;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private PropertyConfig appConfig;

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

    private Map<String,Object> buildField(Field field){
        Map<String,Object> resultMap=new HashMap<>();
        resultMap.put("id",field.getName());
        resultMap.put("name",field.getName());
        resultMap.put("type",field.getType().getSimpleName());
        resultMap.put("label",Globalizer.camelToReadable(field.getName()));
        if(field.isAnnotationPresent(Encrypt.class)){
            resultMap.put("encrypt",true);
        }
        return resultMap;
    }

    private List<Map<String,Object>> buildStructure(Class<?> cls){
        List<Map<String,Object>> result=new ArrayList<>();
        for(Field field:cls.getDeclaredFields()){
            if(field.getName().equals("log"))continue;
            result.add(this.buildField(field));
        }
        return result;
    }

    public List<Map<String,Object>> getSettingStructure() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner=new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(SettingFile.class));
        Set<BeanDefinition> beanSets=scanner.findCandidateComponents("com.sdm");
        List<Map<String,Object>> result=new ArrayList<>();
        for(BeanDefinition bean:beanSets){
            Class<?> cls= Class.forName(bean.getBeanClassName()) ;
            SettingFile settingFile=getSettingFile(cls);

            var structure=this.buildStructure(cls);
            Map<String, Object> structMap=new HashMap<>();
            structMap.put("structure",structure);
            structMap.put("fullName",bean.getBeanClassName());
            structMap.put("name",cls.getSimpleName());
            structMap.put("label",Globalizer.camelToReadable(cls.getSimpleName()));
            structMap.put("icon",settingFile.icon());

            Object data=null;
            try {
                String settings = loadSetting(bean.getBeanClassName());
                data=objectMapper.readValue(settings,cls);
            } catch (IOException e) {
                e.printStackTrace();
            }
            structMap.put("data",data);
            result.add(structMap);
        }
        return result;
    }

    private File createSettingFile(String setting) throws IOException {
        File file = new File(settingRootPath + setting);
        boolean booleanResult = true;
        if (!file.exists()) booleanResult = file.createNewFile();
        return booleanResult ? file : null;
    }

    private SettingFile getSettingFile(Class<?> refClass){
        if (!refClass.isAnnotationPresent(SettingFile.class)) {
            throw new RuntimeException("Can't find setting info.");
        }
        return refClass.getAnnotation(SettingFile.class);
    }

    public SettingFile getSettingFile(String className) throws ClassNotFoundException {
        Class<?> refClass=Class.forName(className);
        return getSettingFile(refClass);
    }

    public String loadSetting(Path settingPath) throws IOException {
        return Files.readString(settingPath);
    }

    public String loadSetting(String className) throws IOException, ClassNotFoundException {
        SettingFile settingFile=this.getSettingFile(className);
        return this.loadSetting(Path.of(settingRootPath, settingFile.value()));
    }

    public <T> T loadSetting(String setting, Class<T> refClass) throws IOException {
        Path settingPath = Path.of(settingRootPath, setting);
        if(!Files.exists(settingPath)){
            try {
                T data = refClass.getDeclaredConstructor().newInstance();
                this.write(setting, data, refClass);
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
        SettingFile settingFile = getSettingFile(refClass);
        return this.loadSetting(settingFile.value(), refClass);
    }

    private void write(String filePath,Object data, Class<?> refClass) throws IOException, IllegalAccessException {
        if(!data.getClass().equals(refClass))
            data=objectMapper.convertValue(data, refClass);

        for(Field field:refClass.getDeclaredFields()){
            if(field.getName().equals("log"))continue;

            Map<String,Object>refField=this.buildField(field);
            field.setAccessible(true);

            String value= (String) field.get(data);
            Boolean isEncrypt=(Boolean)refField.getOrDefault("encrypt",false);
            if(!Globalizer.isNullOrEmpty(value) && !value.startsWith("ENC(") && !value.endsWith(")") && isEncrypt){
                value = "ENC(" + appConfig.stringEncryptor().encrypt(value) + ")";
            }

            field.set(data,value);
        }

        //TODO HERE
        File settingFile = this.createSettingFile(filePath);
        if (!Globalizer.isNullOrEmpty(settingFile)) {
            String settingString = objectMapper.writeValueAsString(data);
            Files.writeString(settingFile.toPath(), settingString, StandardCharsets.UTF_8);
        }
    }

    public void writeSetting(String className, Object data) throws IOException, ClassNotFoundException, IllegalAccessException {
        Class<?> refClass=Class.forName(className);
        SettingFile setting=this.getSettingFile(refClass);
        this.write(setting.value(),data,refClass);
    }

    public <T> void writeSetting(T setting, Class<T> refClass) throws IOException, IllegalAccessException {
        if (!refClass.isAnnotationPresent(SettingFile.class)) {
            throw new RuntimeException("Can't find setting info.");
        }
        SettingFile settingFile = refClass.getAnnotation(SettingFile.class);
        this.write(settingFile.value(), setting,refClass);
    }
}
