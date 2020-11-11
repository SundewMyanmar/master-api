package com.sdm.core.util;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.sdm.core.config.properties.GoogleProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Log4j2
public class GoogleApiManager {
    private final GoogleProperties googleProperties;
    private final JacksonFactory jacksonFactory;

    public GoogleApiManager(GoogleProperties properties) {
        this.googleProperties = properties;
        this.jacksonFactory = new JacksonFactory();
    }

    public Map<String,Object> checkGoogle(String authCode) throws IOException {
        // Set path to the Web application client_secret_*.json file you downloaded from the
        // Google API Console: https://console.developers.google.com/apis/credentials
        // You can also find your Web application client ID and client secret from the
        // console and specify them directly when you create the GoogleAuthorizationCodeTokenRequest
        // object.
        String CLIENT_SECRET_FILE = googleProperties.getAppSecret();

        // Exchange auth code for access token
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JacksonFactory.getDefaultInstance(), new FileReader(CLIENT_SECRET_FILE));
        GoogleTokenResponse tokenResponse =
                new GoogleAuthorizationCodeTokenRequest(
                        new NetHttpTransport(),
                        JacksonFactory.getDefaultInstance(),
                        googleProperties.getTokenServerUrl(),
                        clientSecrets.getDetails().getClientId(),
                        clientSecrets.getDetails().getClientSecret(),
                        authCode,
                        googleProperties.getRedirectUrl())  // Specify the same redirect URI that you use with your web
                        // app. If you don't have a web version of your app, you can
                        // specify an empty string.
                        .execute();

        String accessToken = tokenResponse.getAccessToken();

        // Get profile info from ID token
        GoogleIdToken idToken = tokenResponse.parseIdToken();
        GoogleIdToken.Payload payload = idToken.getPayload();

        Map<String,Object> result=new HashMap<>();
        result.put("userId",payload.getSubject());// Use this value as a key to identify a user.
        result.put("email",payload.getEmail());
        result.put("emailVerified",payload.getEmailVerified());
        result.put("name",(String) payload.get("name"));
        result.put("pictureUrl",(String) payload.get("picture"));
        result.put("locale",(String) payload.get("locale"));
        result.put("familyName",(String) payload.get("family_name"));
        result.put("givenName",(String) payload.get("given_name"));

        return result;
    }
}
