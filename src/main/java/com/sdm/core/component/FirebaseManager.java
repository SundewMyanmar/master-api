/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.component;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.sdm.core.config.properties.FireBaseProperties;
import com.sdm.core.util.MyanmarFontManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author htoonlin
 */
@Component
@Scope("prototype")
public class FirebaseManager {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseManager.class);

    public FirebaseManager(FireBaseProperties fireBaseProperties) {
        try (FileInputStream serviceAccount = new FileInputStream(fireBaseProperties.getServiceJson())) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(fireBaseProperties.getProjectUrl())
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (IOException ex) {
            logger.error(ex.getLocalizedMessage(), ex);
        }
    }

    private AndroidConfig androidNotification(String title, String body) {
        return AndroidConfig.builder()
                .setTtl(3600 * 1000) // 1 hour in milliseconds
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .setSound("default")
                        .build())
                .build();
    }

    private ApnsConfig iosNotification(String title, String body, int badgeCount) {
        return ApnsConfig.builder()
                .putHeader("apns-priority", "10")
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

    public void sendMessage(String token, String title, String body, int badgeCount,
                            Map<String, String> data) {
        String zgTitle = title;
        if (MyanmarFontManager.isMyanmar(title) && !MyanmarFontManager.isZawgyi(title)) {
            zgTitle = MyanmarFontManager.toZawgyi(title);
        }

        String zgBody = body;
        if (MyanmarFontManager.isMyanmar(body) && !MyanmarFontManager.isZawgyi(body)) {
            zgBody = MyanmarFontManager.toZawgyi(body);
        }

        Message message = Message.builder()
                .setToken(token)
                .setNotification(new Notification(zgTitle, zgBody))
                .setApnsConfig(this.iosNotification(zgTitle, zgBody, badgeCount))
                .setAndroidConfig(this.androidNotification(zgTitle, zgBody))
                .putAllData(data)
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            logger.info("Response from FCM => " + response);
        } catch (FirebaseMessagingException ex) {
            logger.warn(ex.getLocalizedMessage());
        }
    }

}
