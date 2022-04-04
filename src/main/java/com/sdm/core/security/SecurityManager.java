/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.security;


import com.sdm.core.config.properties.SecurityProperties;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.service.ISettingManager;
import com.sdm.core.util.Globalizer;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * @author Htoonlin
 */
@Component
@Log4j2
public class SecurityManager {
    @Value("${com.sdm.security.secret-key:N3verG!veupT0Be@Warrior}")
    private String secretKey;

    @Value("${com.sdm.security.secret-iv:UtJnvsgxkVV1vZIQ7A7q8g==}")
    private String secretIv;

    private static final String AES_DEFAULT_ALGORITHM = "AES/CBC/PKCS5Padding";

    @Autowired
    private ISettingManager settingManager;

    public SecurityProperties getProperties() {
        SecurityProperties properties = new SecurityProperties();
        try {
            properties = settingManager.loadSetting(SecurityProperties.class);
        } catch (IOException | IllegalAccessException ex) {
            log.error(ex.getLocalizedMessage());
        }
        return properties;
    }

    public String generateJWTKey() {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        return Base64.getEncoder().encodeToString(key.getEncoded());
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

    public String generateAESKey(int size) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(size);
        SecretKey key = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public String generateAESKey(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), 65536, 256);
        return Base64.getEncoder().encodeToString(factory.generateSecret(spec).getEncoded());
    }

    /**
     * [57, 86, -115, 109, 52, 92, 25, -98, -84, -52, 87, 23, 26, 1, -79, -54]
     *
     * @return
     */
    public String generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return Base64.getEncoder().encodeToString(iv);
    }

    public String aesEncrypt(String input, SecretKey secretKey, String algorithm, IvParameterSpec iv) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            if (Globalizer.isNullOrEmpty(iv)) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            }

            byte[] encryptBytes = input.getBytes(StandardCharsets.UTF_8);
            byte[] byteCipher = cipher.doFinal(encryptBytes);
            return Base64.getEncoder().encodeToString(byteCipher);
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException |
                NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException ex) {
            log.error(ex.getLocalizedMessage(), ex);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    public String aesEncrypt(String input) {
        byte[] decodeKey = Base64.getDecoder().decode(secretKey);
        SecretKey secretKey = new SecretKeySpec(decodeKey, "AES");
        byte[] decodeIv = Base64.getDecoder().decode(secretIv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(decodeIv);
        return this.aesEncrypt(input, secretKey, AES_DEFAULT_ALGORITHM, ivParameterSpec);
    }

    public String aesDecrypt(String input, SecretKey secretKey, String algorithm, IvParameterSpec iv) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            if (Globalizer.isNullOrEmpty(iv)) {
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            }

            byte[] decryptBytes = Base64.getDecoder().decode(input);
            byte[] bytePlain = cipher.doFinal(decryptBytes);
            return new String(bytePlain);
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException |
                NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException ex) {
            log.error(ex.getLocalizedMessage(), ex);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    public String aesDecrypt(String input) {
        byte[] decodeKey = Base64.getDecoder().decode(secretKey);
        SecretKey secretKey = new SecretKeySpec(decodeKey, "AES");
        byte[] decodeIv = Base64.getDecoder().decode(secretIv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(decodeIv);
        return this.aesDecrypt(input, secretKey, AES_DEFAULT_ALGORITHM, ivParameterSpec);
    }
}
