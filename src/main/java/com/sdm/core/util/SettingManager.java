package com.sdm.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.config.PropertyConfig;
import com.sdm.core.service.ISettingManager;
import com.sdm.core.util.annotation.Encrypt;
import com.sdm.core.util.annotation.SettingFile;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.math.NumberUtils;
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
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Log4j2
public class SettingManager implements ISettingManager {
    @Value("${com.sdm.path.setting:/var/www/master-api/setting/}")
    private String settingRootPath;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private PropertyConfig appConfig;

    @Override
    public void init() {
        Path rootPath = Path.of(settingRootPath);
        if (Files.notExists(rootPath)) {
            try {
                Files.createDirectories(rootPath);
            } catch (IOException ex) {
                log.warn(ex.getLocalizedMessage());
            }
        }
    }

    public static void main(String[] args){
        String[] arr=new String[]{"1","2"};
        Boolean isArray=arr.getClass().isArray();
        isArray=isArray;
    }

    private String  getSettingType(Class<?> type){
        if(Globalizer.isNumber(type) || type.equals(Duration.class))return "NUMBER";
        else if(type.equals(Boolean.class) || type.equals(boolean.class)) return "BOOL";
        else if(type.isArray()) return "LIST";
        else if(Collection.class.isAssignableFrom(type) ||
                Map.class.isAssignableFrom(type)||
                Set.class.isAssignableFrom(type))return "MAP";
        else return "TEXT";
    }

    private Object getValue(Field field, Class<?> cls, Object data) throws IllegalAccessException {
        Object value;
        if(cls.equals(data.getClass()))
            value = field.get(data);
        else
            value=((Map<String,Object>)data).get(field.getName());
        return value;
    }

    private void setValue(Field field, Class<?> cls, Object data, Object value) throws IllegalAccessException {
        if(cls.equals(data.getClass()))
            field.set(data, value);
        else
            ((Map<String,Object>)data).put(field.getName(),value);
    }

    private void decryptField(Class<?> cls,Field field,Object data,Map<String,Object> resultMap) throws IllegalAccessException {
        if(field.isAnnotationPresent(Encrypt.class)){
            if(resultMap!=null)
                resultMap.put("encrypt",true);

            Object value=null;
            if(cls.equals(data.getClass()))
                value = field.get(data);
            else
                value=((Map<String,Object>)data).get(field.getName());

            if(value!=null && value.toString().startsWith("ENC(")){
                value=value.toString()
                        .replace("ENC(","")
                        .replace(")","");
                value=appConfig.stringEncryptor().decrypt(value.toString());
            }

            field.set(data,value);
        }
    }

    private Map<String,Object> buildField(Class<?> cls, Field field, Object data) throws IllegalAccessException {
        Map<String,Object> resultMap=new HashMap<>();
        resultMap.put("id",field.getName());
        resultMap.put("name",field.getName());
        resultMap.put("type",getSettingType(field.getType()));
        resultMap.put("label",Globalizer.camelToReadable(field.getName()));
        decryptField(cls,field,data,resultMap);
        return resultMap;
    }

    @Override
    public List<Map<String,Object>> buildStructure(Class<?> cls, Object data) throws IllegalAccessException {
        List<Map<String,Object>> result=new ArrayList<>();
        for(Field field:cls.getDeclaredFields()){
            if(field.getName().equals("log"))continue;

            field.setAccessible(true);

            Map<String,Object> fieldMap=this.buildField(cls, field, data);
            result.add(fieldMap);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getAllSettings() throws ClassNotFoundException, IllegalAccessException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(SettingFile.class));
        Set<BeanDefinition> beanSets = scanner.findCandidateComponents("com.sdm");
        List<Map<String, Object>> result = new ArrayList<>();
        for (BeanDefinition bean : beanSets) {
            Class<?> cls = Class.forName(bean.getBeanClassName());
            SettingFile settingFile = getSettingFile(cls);

            Object data=null;
            try {
                String settings = loadSetting(bean.getBeanClassName());
                data=objectMapper.readValue(settings,cls);
            } catch (IOException e) {
                e.printStackTrace();
            }

            var structure = this.buildStructure(cls, data);
            Map<String, Object> structMap = new HashMap<>();
            structMap.put("structure",structure);
            structMap.put("fullName",bean.getBeanClassName());
            structMap.put("name",cls.getSimpleName());
            structMap.put("label",Globalizer.camelToReadable(cls.getSimpleName()));
            structMap.put("icon",settingFile.icon());
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

    @Override
    public String loadSetting(Path settingPath) throws IOException {
        return Files.readString(settingPath);
    }

    @Override
    public String loadSetting(String className) throws IOException, ClassNotFoundException {
        SettingFile settingFile=this.getSettingFile(className);
        Path settingPath=Path.of(settingRootPath, settingFile.value());
        this.createIfNotExist(settingFile.value(),settingPath,Class.forName(className));
        return this.loadSetting(Path.of(settingRootPath, settingFile.value()));
    }

    private void createIfNotExist(String setting,Path settingPath, Class<?> refClass){
        if(!Files.exists(settingPath)){
            try {
                Object data = refClass.getDeclaredConstructor().newInstance();
                this.write(setting, data, refClass);
            }catch(IOException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex){
                log.warn(ex.getLocalizedMessage());
            }
        }
    }

    @Override
    public <T> T loadSetting(String setting, Class<T> refClass) throws IOException, IllegalAccessException {
        Path settingPath = Path.of(settingRootPath, setting);
        this.createIfNotExist(setting,settingPath,refClass);
        String resultString = this.loadSetting(settingPath);
        if (Globalizer.isNullOrEmpty(resultString))
            resultString = "{}";

        T result=objectMapper.readValue(resultString, refClass);

        for(Field field:refClass.getDeclaredFields()){
            field.setAccessible(true);
            decryptField(refClass,field,result,null);
        }
        return result;
    }

    @Override
    public <T> T loadSetting(Class<T> refClass) throws IOException, IllegalAccessException {
        SettingFile settingFile = getSettingFile(refClass);
        return this.loadSetting(settingFile.value(), refClass);
    }

    private Object write(String filePath,Object data, Class<?> refClass) throws IOException, IllegalAccessException {
        Object newData=Globalizer.clone(data);
        for(Field field:refClass.getDeclaredFields()) {
            if (field.getName().equals("log") || !field.isAnnotationPresent(Encrypt.class))
                continue;

            field.setAccessible(true);
            Object value=this.getValue(field,refClass,newData);

            if (value instanceof Collection) {
                List<String> values = ((Collection<?>) value).stream()
                        .map(Object::toString).collect(Collectors.toList());
                value = String.join(",", values);
            }

            try {
                if (!Globalizer.isNullOrEmpty(value) && !value.toString().startsWith("ENC(")) {
                    String encryptValue = "ENC(" + appConfig.stringEncryptor().encrypt(value.toString()) + ")";
                    this.setValue(field,refClass,newData,encryptValue);
                }
            } catch (Exception ex) {
                log.warn("Invalid value!");
            }
        }

        //TODO HERE
        File settingFile = this.createSettingFile(filePath);
        if (!Globalizer.isNullOrEmpty(settingFile)) {
            String settingString = objectMapper.writeValueAsString(newData);
            Files.writeString(settingFile.toPath(), settingString, StandardCharsets.UTF_8);
        }

        return data;
    }

    @Override
    public Object writeSetting(String className, Object data) throws IOException, ClassNotFoundException, IllegalAccessException {
        Class<?> refClass=Class.forName(className);
        SettingFile setting=this.getSettingFile(refClass);
        return this.write(setting.value(),data,refClass);
    }

    @Override
    public <T> void writeSetting(T setting, Class<T> refClass) throws IOException, IllegalAccessException {
        if (!refClass.isAnnotationPresent(SettingFile.class)) {
            throw new RuntimeException("Can't find setting info.");
        }
        SettingFile settingFile = refClass.getAnnotation(SettingFile.class);
        this.write(settingFile.value(), setting,refClass);
    }
}
