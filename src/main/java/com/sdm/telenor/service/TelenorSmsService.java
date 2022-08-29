package com.sdm.telenor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.HttpResponse;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.HttpRequestManager;
import com.sdm.core.util.ISettingManager;
import com.sdm.telenor.config.properties.TelenorSmsProperties;
import com.sdm.telenor.model.request.telenor.MessageType;
import com.sdm.telenor.model.request.telenor.NameType;
import com.sdm.telenor.model.request.telenor.TelenorSmsMessage;
import com.sdm.telenor.model.request.telenor.TelenorTokenSetting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class TelenorSmsService {
    @Autowired
    private HttpRequestManager httpRequestManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ISettingManager settingManager;

    private TelenorSmsProperties getProperties() {
        TelenorSmsProperties properties = new TelenorSmsProperties();
        try {
            properties = settingManager.loadSetting(TelenorSmsProperties.class);
        } catch (IOException | IllegalAccessException ex) {
            log.error(ex.getLocalizedMessage());
        }
        return properties;
    }

    public TelenorTokenSetting requestCode() throws MalformedURLException, JsonProcessingException {
        URL url = new URL(this.getProperties().getOAuthURL());
        HttpResponse response = httpRequestManager.formGetRequest(url, true);
        log.info("TELENOR_LOAD_TOKEN => " + response.getBody());
        TelenorTokenSetting tokenSetting = objectMapper.readValue(response.getBody(), TelenorTokenSetting.class);

        if (response.getCode() >= 400) {
            throw new GeneralException(HttpStatus.valueOf(response.getCode()), "SMS Error: " + tokenSetting.getStatus());
        }

        return tokenSetting;
    }

    public TelenorTokenSetting requestAccessToken(String authCode) throws IOException, IllegalAccessException {
        /*
          Access Token -> expires_in
          Expires in time in sec. Default is an hour. Max value allowed
          is 86400 seconds (i.e. 24 hours)
         */
        Date expDate = new Date();
        expDate = Globalizer.addSecond(expDate, this.getProperties().getExpSeconds() - 3600);//To Refresh token before 1 hour

        Map<String, String> body = Map.of(
                "client_id", this.getProperties().getClientId(),
                "client_secret", this.getProperties().getClientSecret(),
                "grant_type", "authorization_code",
                "code", authCode,
                "redirect_uri", this.getProperties().getRedirectUri(),
                "expires_in", this.getProperties().getExpSeconds().toString()
        );
        URL url = new URL(this.getProperties().getOAuthTokenURL());
        HttpResponse response = httpRequestManager.formPostRequest(url, body, true);
        log.info("TELENOR_TOKEN =>" + response.getBody());

        TelenorTokenSetting tokenSetting = objectMapper.readValue(response.getBody(), TelenorTokenSetting.class);
        if (response.getCode() >= 400) {
            throw new GeneralException(HttpStatus.valueOf(response.getCode()), "SMS Error: " + tokenSetting.getStatus());
        }

        tokenSetting.setExpiredDate(expDate);
        settingManager.writeSetting(tokenSetting, TelenorTokenSetting.class);

        return tokenSetting;
    }

    /**
     * Currently support only
     * TEXT message
     * ALPHANUMERIC type
     * NATIONAL destination
     *
     * @param content
     * @param phones
     * @return
     */
    private TelenorSmsMessage buildMessage(String content, String[] phones, MessageType msgType) {
        TelenorSmsMessage message = new TelenorSmsMessage();
        message.setType(msgType);
        message.setSendTime(LocalDateTime.now().toString());

        switch (msgType) {
            case TEXT:
                content = Globalizer.encodeUrl(content);
                break;
            default:
                content = DatatypeConverter.printHexBinary(content.getBytes(StandardCharsets.UTF_16));
                break;
        }
        message.setContent(content);

        List<Map<String, String>> characteristics = new ArrayList<>();
        characteristics.add(Map.of("name", "UserName", "value", this.getProperties().getUserName()));
        characteristics.add(Map.of("name", "Password", "value", this.getProperties().getPassword()));
        if (msgType.equals(MessageType.BINARY) || msgType.equals(MessageType.MULTILINGUAL)) {
            characteristics.add(Map.of("name", "Udhi", "value", "1"));
        }
        message.setCharacteristic(characteristics);

        message.setSender(Map.of("@type", NameType.ALPHANUMERIC.getValue(), "name", this.getProperties().getSenderId()));

        List<Map<String, String>> receivers = new ArrayList<>();
        for (String phone : phones) {
            receivers.add(Map.of("@type", NameType.INTERNATIONAL.getValue(), "phoneNumber",
                    this.getProperties().getPhoneNo(Globalizer.cleanPhoneNo(phone))));
        }
        message.setReceiver(receivers);

        return message;
    }

    /**
     * Currently support only
     * TEXT message
     * National destination
     *
     * @param content
     * @param phones
     */
    public Map<String, String> sendMessage(String content, String[] phones, MessageType msgType) throws IOException {
        TelenorTokenSetting setting = new TelenorTokenSetting();
        try {
            setting = settingManager.loadSetting(TelenorTokenSetting.class);
        } catch (IOException | IllegalAccessException ex) {
            log.error(ex.getLocalizedMessage());
        }

        if (setting == null || setting.getAccessToken() == null || setting.getExpiredDate() == null) {
            setting = this.requestCode();
        }

        if (Globalizer.diffSeconds(new Date(), setting.getExpiredDate()) >= 0) {
            setting = this.requestCode();
        }


//        Globalizer.diffSeconds()
        //TODO: test expired
        TelenorSmsMessage message = this.buildMessage(content, phones, msgType);
        return this.sendMessage(message, setting);
    }

    public Map<String, String> sendMessage(TelenorSmsMessage message, TelenorTokenSetting setting) throws IOException {
        if (Globalizer.isNullOrEmpty(setting.getAccessToken())) {
            //TODO: refresh token or schedule refresh token
            return null;
        }

        URL url = new URL(this.getProperties().getCommunicationMessageUrl());
        HttpResponse response = httpRequestManager.jsonPostRequest(url,
                objectMapper.writeValueAsString(message),
                "Bearer " + setting.getAccessToken(),
                true);

        log.info("TELENOR INFO =>" + response.getBody());

        Map<String, String> resultMap = objectMapper.readValue(response.getBody(), Map.class);
        if (resultMap.get("status") == null || !resultMap.get("status").equals("SUCCESS")) {
            log.error("TELENOR_SMS_ERROR =>" + response.getBody());
            throw new GeneralException(HttpStatus.BAD_REQUEST, "SMS Error: " + resultMap.get("message"));
        }

        return resultMap;
    }
}
