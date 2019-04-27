package com.sdm.core.component;


import com.sdm.core.config.FacebookProperties;
import com.sdm.core.exception.GeneralException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FBGraphManager{
    public static final Logger LOG = LoggerFactory.getLogger(FBGraphManager.class);

    private final FacebookProperties properties;

    public FBGraphManager(FacebookProperties properties){
        this.properties = properties;
    }

    public JSONObject checkFacebookToken(String accessToken, String fields, String userAgent){
        //Build Facebook Auth URL
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(this.properties.getGraphURL() + "me")
            .queryParam("fields", fields)
            .queryParam("access_token", accessToken);

        //Build Request Headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent", userAgent);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);

        //Request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> result = restTemplate.exchange(uriBuilder.toUriString(), 
            HttpMethod.GET, requestEntity, String.class);

        if(result.getStatusCode() == HttpStatus.OK){
            return new JSONObject(result.getBody());
        }
        throw new GeneralException(HttpStatus.UNAUTHORIZED, "Invalid Access Token!");
    }

    public ResponseEntity<String> sendMessage(JSONObject entity) {
        //Build Facebook Auth URL
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(this.properties.getGraphURL() + "me/messages")
            .queryParam("access_token", this.properties.getPageAccessToken());

        //Build Request Headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

        LOG.info("Send Message to Facebook => " + entity.toString());

        //Build Request Body
        HttpEntity<String> requestEntity = new HttpEntity<>(entity.toString(), headers);

        //Request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> result = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.POST, 
            requestEntity, String.class);

        LOG.info("Facebook Response => " + result.getBody());
        return result;
    }

}