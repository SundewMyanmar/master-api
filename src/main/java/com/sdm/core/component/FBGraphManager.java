package com.sdm.core.component;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sdm.core.config.FacebookProperties;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.facebook.AttachmentBuilder;
import com.sdm.core.model.facebook.MessageBuilder;
import com.sdm.core.model.facebook.TemplateBuilder;
import com.sdm.core.model.facebook.TextMessageBuilder;

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

    public JsonObject checkFacebookToken(String accessToken, String fields, String userAgent){
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
            return new Gson().fromJson(result.getBody(), JsonObject.class);
        }
        throw new GeneralException(HttpStatus.UNAUTHORIZED, "Invalid Access Token!");
    }

    public ResponseEntity<String> sendMessage(String entity) {
        //Build Facebook Auth URL
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(this.properties.getGraphURL() + "me/messages")
            .queryParam("access_token", this.properties.getPageAccessToken());

        //Build Request Headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

        LOG.info("Send Message to Facebook => " + entity);

        //Build Request Body
        HttpEntity<String> requestEntity = new HttpEntity<>(entity, headers);

        //Request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> result = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.POST, 
            requestEntity, String.class);

        LOG.info("Facebook Response => " + result.getBody());
        return result;
    }

    public ResponseEntity<String> sendMessage(JsonObject json){
        return this.sendMessage(json.getAsString());
    }

    public ResponseEntity<String> sendMessage(MessageBuilder builder){
        return this.sendMessage(builder.build().getAsString());
    }

    public ResponseEntity<String> sendTextMessage(TextMessageBuilder builder){
        return this.sendMessage(builder.build().getAsString());
    }

    public ResponseEntity<String> sendAttachment(AttachmentBuilder builder){
        return this.sendMessage(builder.build().getAsString());
    }

    public ResponseEntity<String> sendTemplate(TemplateBuilder builder){
        return this.sendMessage(builder.build().getAsString());
    }

}