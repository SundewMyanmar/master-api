package com.sdm.notification.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@Service
public class NotificationService {
    private final String PARAM_PATTERN = "\\{[A-Za-z]\\w+\\}";
    private final String PARAM_FILTER_PATTERN = "[A-Za-z]\\w+";

    private List<String> getParams(String rawMessage) {
        Pattern p = Pattern.compile(PARAM_PATTERN);
        Matcher matcher = p.matcher(rawMessage);

        List<String> result = new ArrayList<>();
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    private String getParamField(String param) {
        Pattern p = Pattern.compile(PARAM_FILTER_PATTERN);
        Matcher matcher = p.matcher(param);
        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private Object getValue(Object objectData, String param) {
        try {
            Class<?> cls = objectData.getClass();
            if (cls.getName().equals("java.util.HashMap") || cls.getName().equals("java.util.Map")) {
                Map<String, Object> mapObject = (Map<String, Object>) objectData;
                return mapObject.get(param);
            } else {
                for (Field field : cls.getDeclaredFields()) {
                    if (!field.getName().equals(param)) continue;

                    field.setAccessible(true);
                    return field.get(objectData);
                }
            }
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage());
        }
        return null;
    }

    public String buildMessage(String message, Object objectData) {
        for (String rawParam : getParams(message)) {
            String param = getParamField(rawParam);

            if (param != null) {
                Object value = getValue(objectData, param);
                if (value != null) {
                    message = message.replace(rawParam, value.toString());
                }
            }
        }
        return message;
    }
}
