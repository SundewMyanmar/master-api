/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.security;


import com.sdm.core.config.properties.SecurityProperties;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.util.SettingManager;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

/**
 * @author Htoonlin
 */
@Component
@Log4j2
public class SecurityManager {
    @Value("${com.sdm.security.secret-key}")
    private String secretKey = "";

    @Autowired
    private SettingManager settingManager;

    public SecurityProperties getProperties() {
        SecurityProperties properties = new SecurityProperties();
        try {
            properties = settingManager.loadSetting(SecurityProperties.class);
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
        }
        return properties;
    }

    public String generateSalt() {
        SecureRandom random;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException ex) {
            log.error(ex.getLocalizedMessage(), ex);
            random = new SecureRandom();
        }
        byte[] salt = new byte[64];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public String base64Encode(String normal) {
        byte[] data;
        data = normal.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(data);
    }

    public String base64Decode(String base64) {
        byte[] data = Base64.getDecoder().decode(base64);
        return new String(data);
    }

    public String generateJWTKey() {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        return Encoders.BASE64.encode(key.getEncoded());
    }

    public String generateBase32Secret() {
        StringBuilder sb = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < 16; i++) {
            int val = random.nextInt(32);
            if (val < 26) {
                sb.append((char) ('A' + val));
            } else {
                sb.append((char) ('2' + (val - 26)));
            }
        }
        return sb.toString();
    }

    public String generateHashHmac(String stringData, String secretKey, final String algorithm) {
        try {
            byte[] byteKey = secretKey.getBytes(StandardCharsets.UTF_8);
            Mac sha_hmac = Mac.getInstance(algorithm);
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, algorithm);
            sha_hmac.init(keySpec);
            byte[] mac_data = sha_hmac.
                    doFinal(stringData.getBytes(StandardCharsets.UTF_8));
            return DatatypeConverter.printHexBinary(mac_data);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            log.warn(ex.getLocalizedMessage());
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    private Cipher getAESCipher(int mode, String secret) throws NoSuchAlgorithmException {
        try {
            byte[] key = secret.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(mode, new SecretKeySpec(key, "AES"));
            return cipher;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException ex) {
            log.error(ex.getLocalizedMessage(), ex);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    public String aesEncrypt(String input, String secret) {
        try {
            Cipher cipher = getAESCipher(Cipher.ENCRYPT_MODE, secret);
            byte[] encryptBytes = input.getBytes(StandardCharsets.UTF_8);
            byte[] byteCipher = cipher.doFinal(encryptBytes);
            return Base64.getEncoder().encodeToString(byteCipher);
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException ex) {
            log.error(ex.getLocalizedMessage(), ex);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    public String aesEncrypt(String input) {
        return this.aesEncrypt(input, secretKey);
    }

    public String aesDecrypt(String input, String secret) {
        try {
            Cipher cipher = getAESCipher(Cipher.DECRYPT_MODE, secret);
            byte[] decryptBytes = Base64.getDecoder().decode(input);
            byte[] bytePlain = cipher.doFinal(decryptBytes);
            return new String(bytePlain);
        } catch (GeneralSecurityException ex) {
            log.error(ex.getLocalizedMessage(), ex);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    public String aesDecrypt(String input) {
        return this.aesDecrypt(input, secretKey);
    }
}
