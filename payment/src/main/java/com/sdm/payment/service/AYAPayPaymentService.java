package com.sdm.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.HttpResponse;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.HttpRequestManager;
import com.sdm.core.util.LocaleManager;
import com.sdm.core.util.SettingManager;
import com.sdm.payment.config.properties.AYAPayProperties;
import com.sdm.payment.exception.CallbackException;
import com.sdm.payment.exception.FailedType;
import com.sdm.payment.model.request.ayapay.AYAPaymentRequest;
import com.sdm.payment.model.response.ayapay.AYAMerchangeLoginResponse;
import com.sdm.payment.model.response.ayapay.AYAPayCallbackData;
import com.sdm.payment.model.response.ayapay.AYAPaymentResponse;
import com.sdm.payment.model.response.ayapay.AYATokenResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Service
public class AYAPayPaymentService extends BasePaymentService {
    @Override
    public String getPayment() {
        return "AYA_PAY";
    }

    @Autowired
    private HttpRequestManager httpRequestManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LocaleManager localeManager;

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private SecurityManager aesManager;

    @Autowired
    private SettingManager settingManager;

    private AYAPayProperties getProperties() {
        AYAPayProperties properties = new AYAPayProperties();
        try {
            properties = settingManager.loadSetting(AYAPayProperties.class);
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
        }
        return properties;
    }

    public AYATokenResponse requestToken() throws MalformedURLException, JsonProcessingException {
        String rawUrl = this.getProperties().getPaymentTokenUrl();
        byte[] authToken = (this.getProperties().getConsumerKey() + ":" + this.getProperties().getConsumerSecret()).getBytes(StandardCharsets.UTF_8);
        HttpResponse serverResponse = httpRequestManager.formPostRequest(new URL(rawUrl), "grant_type=client_credentials",
                "Basic " + Base64.getEncoder().encodeToString(authToken), true);
        writeLog(LogType.RESPONSE, serverResponse.getBody());
        AYATokenResponse response = objectMapper.readValue(serverResponse.getBody(), AYATokenResponse.class);
        return response;
    }

    private Map<String, String> buildAccessToken(String token) {
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("Token", token);
        return tokenMap;
    }

    public AYAMerchangeLoginResponse merchantLogin(String token) throws MalformedURLException, JsonProcessingException {
        String rawUrl = this.getProperties().getMerchantLogInUrl();
        Map<String, String> body = new HashMap<>();
        body.put("phone", this.getProperties().getPhone());
        body.put("password", this.getProperties().getPin());
        HttpResponse serverResponse = httpRequestManager.jsonPostRequest(new URL(rawUrl),
                objectMapper.writeValueAsString(body), null, true, buildAccessToken(token));
        writeLog(LogType.RESPONSE, serverResponse.getBody());
        AYAMerchangeLoginResponse response = objectMapper.readValue(serverResponse.getBody(), AYAMerchangeLoginResponse.class);

        if (!response.getErr().equals("200")) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, "Invalid Payment Server Response!");
        }

        return response;
    }

    private AYAPaymentRequest buildPaymentJson(String phone, String orderId, String orderDetails, String amount) {
        AYAPaymentRequest request = new AYAPaymentRequest();
        request.setCustomerPhone(this.getProperties().getPhoneNo(Globalizer.cleanPhoneNo(phone)));
        request.setAmount(amount);
        request.setCurrency(this.getProperties().getCurrency());
        request.setExternalTransactionId(orderId);
        request.setExternalAdditionalData(orderDetails);

        return request;
    }

    public AYAPaymentResponse paymentRequest(String phone, String orderId, String orderDetails, String amount) {
        try {
            AYATokenResponse tokenResponse = this.requestToken();
            String accessToken = tokenResponse.getTokenType() + " " + tokenResponse.getAccessToken();

            AYAMerchangeLoginResponse loginResponse = this.merchantLogin(accessToken);
            Map<String, String> tokenMap = loginResponse.getToken();
            String token = tokenMap.get("token");
            String rawUrl = this.getProperties().getPaymentOrderUrl();

            HttpResponse serverResponse = httpRequestManager.jsonPostRequest(new URL(rawUrl),
                    objectMapper.writeValueAsString(buildPaymentJson(phone, orderId, orderDetails, amount)),
                    "Bearer " + token,
                    true,
                    buildAccessToken(accessToken));
            AYAPaymentResponse response = objectMapper.readValue(serverResponse.getBody(), AYAPaymentResponse.class);
            if (!response.getErr().equals("200")) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, "Invalid Payment Server Response!");
            }

            return response;
        } catch (MalformedURLException | JsonProcessingException ex) {
            writeLog(LogType.ERROR, ex.getLocalizedMessage());
            throw new GeneralException(HttpStatus.BAD_REQUEST, getPayment() + " : " + ex.getLocalizedMessage());
        }
    }

    public AYAPayCallbackData decryptPaymentResult(String paymentResult) throws CallbackException {
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            byte[] keyBytes = this.getProperties().getDecryptionKey().getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decryptBytes = Base64.getDecoder().decode(paymentResult);
            String decryptedJson = new String(cipher.doFinal(decryptBytes));
            return objectMapper.readValue(decryptedJson, AYAPayCallbackData.class);
        } catch (JsonProcessingException | GeneralSecurityException ex) {
            writeLog(LogType.ERROR, ex.getLocalizedMessage());
            throw new CallbackException(FailedType.RUNTIME_ERROR, ex.getLocalizedMessage(), ex);
        }
    }
}
