package com.sdm.core.component;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sdm.core.config.FacebookProperties;
import com.sdm.core.exception.GeneralException;

import com.sdm.core.security.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FBGraphManager{
    public static final Logger LOG = LoggerFactory.getLogger(FBGraphManager.class);

    private final FacebookProperties properties;

    @Autowired
    SecurityManager securityManager;

    public FBGraphManager(FacebookProperties properties){
        this.properties = properties;
    }

    public JsonObject checkFacebookToken(String accessToken, String fields, String userAgent){
        //Build Facebook Auth URL
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(this.properties.getGraphURL() + "me")
            .queryParam("fields", fields)
            .queryParam("access_token", accessToken);

        //Build Request Headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent", userAgent);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);

        //Request
        RestTemplate restTemplate = new RestTemplate();
        String str=uriBuilder.toUriString();
        ResponseEntity<String> result = restTemplate.exchange(uriBuilder.toUriString(), 
            HttpMethod.GET, requestEntity, String.class);

        if(result.getStatusCode() == HttpStatus.OK){
            return new Gson().fromJson(result.getBody(), JsonObject.class);
        }
        throw new GeneralException(HttpStatus.UNAUTHORIZED, "Invalid Access Token!");
    }

    public JsonObject retrievePSUserData(String psId, String fields,String userAgent){
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
        RestTemplate restTemplate = new RestTemplate();
        String str=uriBuilder.toUriString();
        ResponseEntity<String> result = restTemplate.exchange(uriBuilder.toUriString(),
                HttpMethod.GET, requestEntity, String.class);

        if(result.getStatusCode() == HttpStatus.OK){
            return new Gson().fromJson(result.getBody(), JsonObject.class);
        }
        throw new GeneralException(HttpStatus.UNAUTHORIZED, "Invalid PSID!");
    }

    public ResponseEntity<String> sendMessage(JsonObject entity) {
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

    /*
    Reference Url : https://developers.facebook.com/docs/graph-api/securing-requests/#appsecret_proof
    * */
    public String generateAppSecretProof(){
        return securityManager.generateHashHmac(properties.getPageAccessToken(),properties.getAppSecret());
    }
}