/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.security;

import com.sdm.core.exception.GeneralException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/**
 * @author Htoonlin
 */
@Component
@Log4j2
public class AESManager {

    private final String MESSAGE_DIGEST = "SHA-1";
    private final String CRYPTO_METHOD = "AES";
    private final String CRYPTO_PAIR = "AES/ECB/PKCS5Padding";

    public SecretKeySpec generateKey(String secret) throws NoSuchAlgorithmException {
        byte[] key;

        try {
            MessageDigest sha = null;
            key = secret.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance(MESSAGE_DIGEST);
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            return new SecretKeySpec(key, CRYPTO_METHOD);
        } catch (NoSuchAlgorithmException ex) {
            log.error(ex.getLocalizedMessage(), ex);
            throw ex;
        }
    }

    public String encrypt(String plainText, String key) throws GeneralException {
        try {
            SecretKeySpec secretKey = generateKey(key);
            Cipher cipher = Cipher.getInstance(CRYPTO_PAIR);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptBytes = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] byteCipher = cipher.doFinal(encryptBytes);
            return Base64.getEncoder().encodeToString(byteCipher);
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException ex) {
            log.error(ex.getLocalizedMessage(), ex);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    public String decrypt(String cipherText, String key) throws GeneralException {
        try {
            SecretKeySpec secretKey = generateKey(key);
            Cipher cipher = Cipher.getInstance(CRYPTO_PAIR);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decryptBytes = Base64.getDecoder().decode(cipherText);
            byte[] bytePlain = cipher.doFinal(decryptBytes);
            return new String(bytePlain);
        } catch (GeneralSecurityException ex) {
            log.error(ex.getLocalizedMessage(), ex);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }
}
