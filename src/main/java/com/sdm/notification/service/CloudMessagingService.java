/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.notification.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.sdm.admin.model.User;
import com.sdm.auth.model.Token;
import com.sdm.auth.repository.TokenRepository;
import com.sdm.core.util.Globalizer;
import com.sdm.notification.config.properties.FireBaseProperties;
import com.sdm.notification.model.Notification;
import com.sdm.notification.repository.NotificationRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author htoonlin
 */
@Service
@Log4j2
public class CloudMessagingService {

    private static final String FIR_APP_NAME = "FIR_CLOUD_MESSAGING_APP";
    private static final String ANDROID_COLOR = "#ffffff";
    private static final String ANDROID_ICON = "noti";

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private NotificationRepository repository;

    private FirebaseApp firebaseApp;

    public CloudMessagingService(FireBaseProperties properties) {
        if (properties.getProjectUrl().length() > 0 && properties.getServiceJson().length() > 0) {
            if (firebaseApp == null) {
                try (FileInputStream serviceAccount = new FileInputStream(properties.getServiceJson())) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .setDatabaseUrl(properties.getProjectUrl())
                            .build();
                    this.firebaseApp = FirebaseApp.initializeApp(options, FIR_APP_NAME);
                } catch (IOException ex) {
                    log.warn(ex.getLocalizedMessage(), ex);
                }
            }
        }
    }

    private WebpushConfig webpushConfig(Integer badgeCount, String category) {
        WebpushNotification.Builder builder = WebpushNotification.builder();
        if (!StringUtils.isEmpty(category)) {
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
        if (!StringUtils.isEmpty(ANDROID_COLOR)) {
            builder.setColor(ANDROID_COLOR);
        }
        if (!StringUtils.isEmpty(ANDROID_ICON)) {
            builder.setIcon(ANDROID_ICON);
        }
        if (!StringUtils.isEmpty(category)) {
            builder.setTag(category);
        }

        return AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.NORMAL)
                .setNotification(builder.build())
                .build();
    }

    private ApnsConfig iosConfig(int badgeCount, String category) {
        Aps.Builder builder = Aps.builder();
        if (!StringUtils.isEmpty(category)) {
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

    public List<String> getFcmTokens(User user) {
        if (user.getId() == null || user.getId() <= 0) {
            return List.of();
        }
        return tokenRepository.findByUserId(user.getId())
                .orElse(List.of()).stream()
                .map(Token::getFirebaseMessagingToken)
                .filter(t -> !Globalizer.isNullOrEmpty(t))
                .collect(Collectors.toList());
    }

    public void sendUserMessage(Notification notification) {
        List<String> fcmTokens = getFcmTokens(notification.getUser());
        if (fcmTokens != null && fcmTokens.size() <= 0)
            return;

        Integer badgeCount = repository.unreadCount(notification.getUser().getId());

        MulticastMessage message = MulticastMessage.builder()
                .putAllData(notification.getData())
                .setNotification(notification.buildFCM())
                .setWebpushConfig(webpushConfig(badgeCount + 1, notification.getCategory()))
                .setAndroidConfig(androidConfig(notification.getCategory()))
                .setApnsConfig(iosConfig(badgeCount + 1, notification.getCategory()))
                .addAllTokens(fcmTokens)
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance(firebaseApp).sendMulticast(message);

            //Save Notification Info
            notification.setId(UUID.randomUUID().toString());
            notification.setSentCount(response.getSuccessCount());
            notification.setSentAt(new Date());
            notification.setTopic(null);
            repository.save(notification);
        } catch (FirebaseMessagingException e) {
            log.warn(e.getLocalizedMessage());
        }
    }

    public void sendTopicMessage(Notification notification) {
        Message message = Message.builder()
                .putAllData(notification.getData())
                .setNotification(notification.buildFCM())
                .setAndroidConfig(androidConfig(notification.getCategory()))
                .setApnsConfig(iosConfig(0, notification.getCategory()))
                .setTopic(notification.getTopic())
                .build();
        try {
            FirebaseMessaging.getInstance(firebaseApp).send(message);

            //Save Notification Info
            notification.setId(UUID.randomUUID().toString());
            notification.setSentAt(new Date());
            notification.setUser(null);
            repository.save(notification);
        } catch (FirebaseMessagingException e) {
            log.warn(e.getLocalizedMessage());
        }
    }

    @Async
    public void sendMessage(final Notification notification) {
        CompletableFuture.runAsync(() -> {
            if (notification.getUser() != null && notification.getUser().getId() > 0) {
                sendUserMessage(notification);
            }

            if (!Globalizer.isNullOrEmpty(notification.getTopic())) {
                sendTopicMessage(notification);
            }
        });
    }

}
