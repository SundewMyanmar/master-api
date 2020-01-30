/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.util.firebase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.sdm.core.config.properties.FireBaseProperties;
import com.sdm.core.util.MyanmarFontManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author htoonlin
 */
@Component
public class CloudMessagingManager {

    private static final Logger logger = LoggerFactory.getLogger(CloudMessagingManager.class);

    private static final String FIR_APP_NAME = "FIR_CLOUD_MESSAGING_APP";

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    private FirebaseApp defaultApp;

    public CloudMessagingManager(FireBaseProperties fireBaseProperties) {
        if (fireBaseProperties.getProjectUrl().length() > 0 && fireBaseProperties.getServiceJson().length() > 0) {
            defaultApp = FirebaseApp.getInstance(FIR_APP_NAME);
            if (defaultApp == null) {
                try (FileInputStream serviceAccount = new FileInputStream(fireBaseProperties.getServiceJson())) {
                    FirebaseOptions options = new FirebaseOptions.Builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .setDatabaseUrl(fireBaseProperties.getProjectUrl())
                            .build();
                    defaultApp = FirebaseApp.initializeApp(options, FIR_APP_NAME);
                } catch (IOException ex) {
                    logger.warn(ex.getLocalizedMessage(), ex);
                }
            }
        }
    }

    private AndroidConfig androidConfig(String title, String body, int badgeCount, Map<String, String> data) {
        data.put("badgeCount", String.valueOf(badgeCount));

        return AndroidConfig.builder()
                .setTtl(3600 * 1000) // 1 hour in milliseconds
                .setPriority(AndroidConfig.Priority.HIGH)
                .putAllData(data)
                .setNotification(AndroidNotification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .setSound("default")
                        .build())
                .build();
    }

    private ApnsConfig iosConfig(String title, String body, int badgeCount, Map<String, Object> data) {
        return ApnsConfig.builder()
                .putHeader("apns-priority", "10")
                .putAllCustomData(data)
                .setAps(Aps.builder()
                        .setAlert(ApsAlert.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .setSound("default")
                        .setBadge(badgeCount)
                        .build())
                .build();
    }

    private Map<String, String> convertToStringData(Map<String, Object> data) {
        Map<String, String> stringData = new HashMap<>();
        data.forEach((key, value) -> {
            if (value instanceof Map) {
                try {
                    stringData.put(key, jacksonObjectMapper.writeValueAsString(value));
                } catch (JsonProcessingException ex) {
                    logger.warn(ex.getLocalizedMessage());
                    stringData.put(key, value.toString());
                }
            } else {
                stringData.put(key, value.toString());
            }

        });
        return stringData;
    }

    public Notification buildNotification(String title, String body) {
        return Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();
    }

    public ApiFuture<String> sendMessage(String token, String title, String body, int badgeCount,
                                         Map<String, Object> data) {

        title = MyanmarFontManager.toUnicode(title);
        body = MyanmarFontManager.toUnicode(body);

        Map<String, String> stringData = this.convertToStringData(data);

        Message message = Message.builder()
                .setToken(token)
                .setNotification(this.buildNotification(title, body))
                .setAndroidConfig(this.androidConfig(title, body, badgeCount, stringData))
                .setApnsConfig(this.iosConfig(title, body, badgeCount, data))
                .putAllData(stringData).build();

        return FirebaseMessaging.getInstance().sendAsync(message);
    }


    public ApiFuture<BatchResponse> sendMessage(List<String> tokens, String title, String body, int badgeCount,
                                                Map<String, Object> data) {

        title = MyanmarFontManager.toUnicode(title);
        body = MyanmarFontManager.toUnicode(body);

        Map<String, String> stringData = this.convertToStringData(data);
        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(this.buildNotification(title, body))
                .setAndroidConfig(this.androidConfig(title, body, badgeCount, stringData))
                .setApnsConfig(this.iosConfig(title, body, badgeCount, data))
                .putAllData(stringData).build();

        return FirebaseMessaging.getInstance().sendMulticastAsync(message);

    }

}
