/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.security;


import com.sdm.core.config.properties.SecurityProperties;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Random;

/**
 * @author Htoonlin
 */
@Component
@Log4j2
public class SecurityManager {

    @Autowired
    private SecurityProperties securityProperties;

    public SecurityProperties getProperties() {
        return this.securityProperties;
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

    public String hashString(String input) {
        String systemSalt = this.securityProperties.getEncryptSalt();

        log.info("Preparing to encrypt data....");
        final int iterations = 1000;
        final int keyLength = 512;
        char[] password = input.toCharArray();
        byte[] staticSalt = Base64.getDecoder().decode(systemSalt);
        try {
            PBEKeySpec spec = new PBEKeySpec(password, staticSalt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            String inputHex = DatatypeConverter.printHexBinary(skf.generateSecret(spec).getEncoded());
            log.info("Successfully encrypted data.");
            return inputHex;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error(e.getLocalizedMessage(), e);
        }

        return base64Encode(input + systemSalt);
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
        Mac sha_hmac = null;
        try {
            byte[] byteKey = secretKey.getBytes(StandardCharsets.UTF_8);
            sha_hmac = Mac.getInstance(algorithm);
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, algorithm);
            sha_hmac.init(keySpec);
            byte[] mac_data = sha_hmac.
                    doFinal(stringData.getBytes(StandardCharsets.UTF_8));
            return DatatypeConverter.printHexBinary(mac_data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.warn(e.getLocalizedMessage());
        }
        return null;
    }
}
