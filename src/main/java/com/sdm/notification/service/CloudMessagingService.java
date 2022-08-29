/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.notification.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;
import com.sdm.core.util.Globalizer;
import com.sdm.notification.model.Notification;
import com.sdm.notification.repository.NotificationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import lombok.extern.log4j.Log4j2;

/**
 * @author htoonlin
 */
@Service
@Log4j2
public class CloudMessagingService {

    private static final String FIREBASE_JSON_FILE_NAME = "classpath:firebase.json";
    private static final String FIR_APP_NAME = "FIR_CLOUD_MESSAGING_APP";
    private static final String ANDROID_COLOR = "#ffffff";
    private static final String ANDROID_ICON = "noti";

    @Autowired
    private NotificationRepository repository;

    private FirebaseApp firebaseApp;

    @PostConstruct
    public void init() {
        try {
            File jsonFile = ResourceUtils.getFile(FIREBASE_JSON_FILE_NAME);
            try (FileInputStream jsonStream = new FileInputStream(jsonFile)) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(jsonStream))
                        .build();
                this.firebaseApp = FirebaseApp.initializeApp(options, FIR_APP_NAME);
            } catch (IOException ex) {
                log.warn(ex.getLocalizedMessage(), ex);
            }
        } catch (FileNotFoundException ex) {
            log.warn(ex.getLocalizedMessage());
        }
    }

    private WebpushConfig webpushConfig(Integer badgeCount, String category) {
        WebpushNotification.Builder builder = WebpushNotification.builder();
        if (!Globalizer.isNullOrEmpty(category)) {
            builder.setTag(category);
        }

        if (badgeCount > 0) {
            builder.setBadge(badgeCount.toString());
        }

        return WebpushConfig.builder()
                .setNotification(builder.build())
                .build();
    }

    private AndroidConfig androidConfig(String category) {
        AndroidNotification.Builder builder = AndroidNotification.builder();
        if (!Globalizer.isNullOrEmpty(ANDROID_COLOR)) {
            builder.setColor(ANDROID_COLOR);
        }
        if (!Globalizer.isNullOrEmpty(ANDROID_ICON)) {
            builder.setIcon(ANDROID_ICON);
        }
        if (!Globalizer.isNullOrEmpty(category)) {
            builder.setTag(category);
        }

        return AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.NORMAL)
                .setNotification(builder.build())
                .build();
    }

    private ApnsConfig iosConfig(int badgeCount, String category) {
        Aps.Builder builder = Aps.builder();
        if (!Globalizer.isNullOrEmpty(category)) {
            builder.setCategory(category);
        }

        if (badgeCount > 0) {
            builder.setBadge(badgeCount);
        }

        return ApnsConfig.builder()
                .putHeader("apns-priority", "10")
                .setAps(builder.build())
                .build();
    }

    @Transactional
    public void sendMessageByTokens(com.sdm.notification.model.Notification notification, List<String> fcmTokens) {
        if (fcmTokens != null && fcmTokens.size() <= 0)
            return;

        Integer badgeCount = repository.unreadCount(notification.getUserId());

        MulticastMessage message = MulticastMessage.builder()
                .putAllData(notification.getData())
                .setNotification(notification.buildFCM())
                .setWebpushConfig(webpushConfig(badgeCount + 1, notification.getCategory()))
                .setAndroidConfig(androidConfig(notification.getCategory()))
                .setApnsConfig(iosConfig(badgeCount + 1, notification.getCategory()))
                .addAllTokens(fcmTokens)
                .build();

        try {
            log.info(String.format("Sending message to [%d] => %s", notification.getUserId(), notification.getTitle()));
            BatchResponse responses = FirebaseMessaging.getInstance(firebaseApp).sendMulticast(message);
            log.info("Success message count => {}", responses.getSuccessCount());
            log.info("Failure message count => {}", responses.getFailureCount());
            List<String> successIds = new ArrayList<>();
            for (SendResponse response : responses.getResponses()) {
                if (response.isSuccessful()) {
                    successIds.add(response.getMessageId());
                }
            }
            notification.setFcmIds(String.join(",", successIds));
            notification.setId(UUID.randomUUID().toString());
            notification.setSentAt(new Date());
            notification.setTopic(null);
            repository.save(notification);

        } catch (FirebaseMessagingException e) {
            log.warn(e.getLocalizedMessage());
        }
    }

    @Transactional
    public void sendTopicMessage(Notification notification, String topic) {
        Message message = Message.builder()
                .putAllData(notification.getData())
                .setNotification(notification.buildFCM())
                .setAndroidConfig(androidConfig(notification.getCategory()))
                .setApnsConfig(iosConfig(0, notification.getCategory()))
                .setTopic(topic)
                .build();
        try {
            log.info(String.format("Sending message to [%s] => %s", notification.getTopic(), notification.getTitle()));
            String messageId = FirebaseMessaging.getInstance(firebaseApp).send(message);

            //Save Notification Info
            notification.setId(UUID.randomUUID().toString());
            notification.setFcmIds(messageId);
            notification.setSentAt(new Date());
            notification.setUserId(0);
            notification.setTopic(topic);
            repository.save(notification);
        } catch (FirebaseMessagingException e) {
            log.warn(e.getLocalizedMessage());
        }
    }
}
