package com.sdm.core.util;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sdm.core.config.properties.FacebookProperties;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.security.SecurityManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Component
@Log4j2
public class FBGraphManager {
    private final FacebookProperties properties;

    @Autowired
    private SecurityManager securityManager;

    private final RestTemplate restTemplate;

    public FBGraphManager(FacebookProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    public JsonObject checkFacebookToken(String accessToken, String fields) throws IOException {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(this.properties.getGraphURL() + "me")
                .queryParam("fields", fields)
                .queryParam("access_token", accessToken);
        URL API_URL = new URL(uriBuilder.toUriString());

        HttpURLConnection connection = (HttpURLConnection) API_URL.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");

        if (connection.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + connection.getResponseCode());
        }

        BufferedReader RESPONSE_BUFFER = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder result = new StringBuilder();
        String output;

        while ((output = RESPONSE_BUFFER.readLine()) != null) {
            result.append(output);
        }
        RESPONSE_BUFFER.close();
        connection.disconnect();

        return new Gson().fromJson(result.toString(), JsonObject.class);
    }

    public JsonObject retrievePSUserData(String psId, String fields, String userAgent) {
        //Build Facebook Auth URL
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(this.properties.getGraphURL() + psId)
                .queryParam("fields", fields)
                .queryParam("access_token", this.properties.getPageAccessToken());

        //Build Request Headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent", userAgent);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);

        //Request
        String str = uriBuilder.toUriString();
        ResponseEntity<String> result = restTemplate.exchange(uriBuilder.toUriString(),
                HttpMethod.GET, requestEntity, String.class);

        if (result.getStatusCode() == HttpStatus.OK) {
            return new Gson().fromJson(result.getBody(), JsonObject.class);
        }
        throw new GeneralException(HttpStatus.UNAUTHORIZED, "Invalid PSID!");
    }

    public ResponseEntity<String> sendWelcomeScreen(JsonObject entity) {
        //Build Facebook Auth URL
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(this.properties.getGraphURL() + "me/messenger_profile")
                .queryParam("access_token", this.properties.getPageAccessToken());

        //Build Request Headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

        log.info("Send Welcome Screen Message to Facebook => " + entity.toString());
        //Build Request Body
        HttpEntity<String> requestEntity = new HttpEntity<>(entity.toString(), headers);

        //Request
        ResponseEntity<String> result = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.POST,
                requestEntity, String.class);

        log.info("Facebook Response => " + result.getBody());
        return result;
    }

    public ResponseEntity<String> sendMessage(JsonObject entity) {
        //Build Facebook Auth URL
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(this.properties.getGraphURL() + "me/messages")
                .queryParam("access_token", this.properties.getPageAccessToken());

        //Build Request Headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

        log.info("Send Message to Facebook => " + entity.toString());

        //Build Request Body
        HttpEntity<String> requestEntity = new HttpEntity<>(entity.toString(), headers);

        //Request
        ResponseEntity<String> result = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.POST,
                requestEntity, String.class);

        log.info("Facebook Response => " + result.getBody());
        return result;
    }

    /*
    Reference Url : https://developers.facebook.com/docs/graph-api/securing-requests/#appsecret_proof
    * */
    public String generateAppSecretProof() {
        return securityManager.generateHashHmac(properties.getPageAccessToken(), properties.getAppSecret());
    }
}