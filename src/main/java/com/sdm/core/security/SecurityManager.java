/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.security;


import com.sdm.core.config.properties.SecurityProperties;
import com.sdm.core.util.Globalizer;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * @author Htoonlin
 */
@Component
public class SecurityManager {

    private static final Logger logger = LoggerFactory.getLogger(SecurityManager.class);

    @Autowired
    private SecurityProperties securitySettings;

    public String generateSalt() {
        SecureRandom random;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException ex) {
            logger.error(ex.getLocalizedMessage(), ex);
            random = new SecureRandom();
        }
        byte salt[] = new byte[64];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public String hashString(String input) {
        String systemSalt = this.securitySettings.getEncryptSalt();

        logger.info("Preparing to encrypt data....");
        final int iterations = 1000;
        final int keyLength = 512;
        char[] password = input.toCharArray();
        byte[] staticSalt = Base64.getDecoder().decode(systemSalt);
        try {
            PBEKeySpec spec = new PBEKeySpec(password, staticSalt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            String inputHex = DatatypeConverter.printHexBinary(skf.generateSecret(spec).getEncoded());
            logger.info("Successfully encrypted data.");
            return inputHex;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return base64Encode(input + systemSalt);
    }

    public String randomPassword(int length) {
        String passwordChars = "ABCDEFGHIJKLMNOPQRSTUVWHZ";
        passwordChars += passwordChars.toLowerCase();
        passwordChars += "0123456789";
        passwordChars += "!@#$%^&*()_+-=";
        return Globalizer.generateToken(passwordChars, length);
    }

    public String base64Encode(String normal) {
        byte[] data;
        try {
            data = normal.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            data = normal.getBytes(Charset.defaultCharset());
        }
        return Base64.getEncoder().encodeToString(data);
    }

    public String base64Decode(String base64) {
        byte[] data = Base64.getDecoder().decode(base64);
        return new String(data);
    }

    public String generateJWTKey() {
        byte[] key = MacProvider.generateKey().getEncoded();
        return Base64.getEncoder().encodeToString(key);
    }

    public String generateHashHmac(String stringData, String secretKey) {
        Mac sha512_HMAC = null;
        try {
            byte[] byteKey = secretKey.getBytes("UTF-8");
            final String HMAC_SHA512 = "HmacSHA512";
            sha512_HMAC = Mac.getInstance(HMAC_SHA512);
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA512);
            sha512_HMAC.init(keySpec);
            byte[] mac_data = sha512_HMAC.
                    doFinal(stringData.getBytes("UTF-8"));
            //result = Base64.encode(mac_data);
            return bytesToHex(mac_data);
        } catch (UnsupportedEncodingException e) {

        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
