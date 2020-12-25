package com.sdm.core.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.sdm.core.config.properties.GoogleProperties;
import com.sdm.core.exception.GeneralException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Component
@Log4j2
public class GoogleApiManager {
    private final GoogleProperties googleProperties;

    public GoogleApiManager(GoogleProperties properties) {
        this.googleProperties = properties;
    }

    /**
     * Check Google Account
     * Set path to the Web application client_secret_*.json file you downloaded from the
     * Google API Console: https://console.developers.google.com/apis/credentials
     * You can also find your Web application client ID and client secret from the
     * console and specify them directly when you create the GoogleAuthorizationCodeTokenRequest object.
     *
     * @param authCode
     * @return
     * @throws IOException
     */
    public GoogleIdToken.Payload checkGoogle(String authCode) {
        try (FileReader secretReader = new FileReader(googleProperties.getAppSecret())) {
            GoogleClientSecrets clientSecret = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), secretReader);
            NetHttpTransport transport = new NetHttpTransport();
            JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jacksonFactory)
                    .setAudience(List.of(clientSecret.getDetails().getClientId()))
                    .build();
            GoogleIdToken idToken = verifier.verify(authCode);
            if (idToken == null) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, "Invalid google access token.");
            }

            return idToken.getPayload();
        } catch (IOException | GeneralSecurityException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }
}
