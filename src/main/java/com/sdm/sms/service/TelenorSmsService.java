package com.sdm.sms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.HttpRequestManager;
import com.sdm.core.util.SettingManager;
import com.sdm.sms.config.properties.TelenorSmsProperties;
import com.sdm.sms.model.request.telenor.MessageType;
import com.sdm.sms.model.request.telenor.NameType;
import com.sdm.sms.model.request.telenor.TelenorSmsMessage;
import com.sdm.sms.model.request.telenor.TelenorTokenSetting;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Log4j2
public class TelenorSmsService {
    private final String SETTING_FILE = "telenor-sms-setting.json";
    @Autowired
    private HttpRequestManager httpRequestManager;
    @Autowired
    private TelenorSmsProperties telenorSmsProperties;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SettingManager<TelenorTokenSetting> settingManager;

    public TelenorTokenSetting requestCode() throws MalformedURLException, JsonProcessingException {
        URL url = new URL(telenorSmsProperties.getOAuthURL());
        String result = httpRequestManager.formGetRequest(url, true);
        TelenorTokenSetting tokenSetting = objectMapper.readValue(result, TelenorTokenSetting.class);
        log.info("TELENOR_LOAD_TOKEN => " + result);

        return tokenSetting;
    }

    public TelenorTokenSetting requestAccessToken(String authCode) throws IOException {
        /**
         * Access Token -> expires_in
         * Expires in time in sec. Default is an hour. Max value allowed
         * is 86400 seconds (i.e. 24 hours)
         */
        Date expDate = new Date();
        expDate = Globalizer.addSecond(expDate, telenorSmsProperties.getExpiredIn() - 3600);//To Refresh token before 1 hour

        Map<String, String> body = Map.of(
                "client_id", telenorSmsProperties.getClientId(),
                "client_secret", telenorSmsProperties.getClientSecret(),
                "grant_type", "authorization_code",
                "code", authCode,
                "redirect_uri", telenorSmsProperties.getRedirectUri(),
                "expires_in", telenorSmsProperties.getExpiredIn().toString()
        );
        URL url = new URL(telenorSmsProperties.getOAuthTokenURL());
        String result = httpRequestManager.formPostRequest(url, body, true);

        TelenorTokenSetting tokenSetting = objectMapper.readValue(result, TelenorTokenSetting.class);

        tokenSetting.setExpiredDate(expDate);
        log.info("TELENOR_TOKEN =>" + result);
        settingManager.writeSetting(SETTING_FILE, tokenSetting);

        return tokenSetting;
    }

    private Map<String, String> buildMapData(String key1, String value1, String key2, String value2) {
        Map<String, String> data = new HashMap<>();
        data.put(key1, value1);
        data.put(key2, value2);
        return data;
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
                content = Hex.encodeHexString(content.getBytes(StandardCharsets.UTF_16));
                break;
        }
        message.setContent(content);

        List<Map<String, String>> characteristics = new ArrayList<>();
        characteristics.add(this.buildMapData("name", "UserName", "value", telenorSmsProperties.getUserName()));
        characteristics.add(this.buildMapData("name", "Password", "value", telenorSmsProperties.getPassword()));
        message.setCharacteristic(characteristics);

        message.setSender(this.buildMapData("@type", NameType.ALPHANUMERIC.getValue(), "name", telenorSmsProperties.getSenderId()));

        List<Map<String, String>> receivers = new ArrayList<>();
        for (String phone : phones) {
            receivers.add(this.buildMapData("@type", NameType.INTERNATIONAL.getValue(), "phoneNumber",
                    telenorSmsProperties.getPhoneNo(Globalizer.cleanPhoneNo(phone))));
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
        TelenorTokenSetting setting = settingManager.loadSetting(SETTING_FILE, TelenorTokenSetting.class);
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

        URL url = new URL(telenorSmsProperties.getCommunicationMessageUrl());
        String result = httpRequestManager.jsonPostRequest(url,
                objectMapper.writeValueAsString(message),
                "Bearer " + setting.getAccessToken(),
                true);

        log.info("TELENOR INFO =>" + result);
        Map<String, String> resultMap = objectMapper.readValue(result, Map.class);
        if (resultMap.get("status") == null || !resultMap.get("status").equals("SUCCESS")) {
            log.error("TELENOR_SMS_ERROR =>" + result);
            throw new GeneralException(HttpStatus.BAD_REQUEST, resultMap.get("message"));
        }

        return resultMap;
    }
}
