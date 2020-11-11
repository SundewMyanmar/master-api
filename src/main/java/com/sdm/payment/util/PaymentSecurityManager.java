package com.sdm.payment.util;

import com.sdm.payment.config.properties.AGDProperties;
import com.sdm.payment.config.properties.MPUProperties;
import com.sdm.payment.config.properties.UABProperties;
import com.sdm.payment.config.properties.YOMAProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

@Component
@Log4j2
public class PaymentSecurityManager {
    @Autowired
    private UABProperties uabProperties;

    @Autowired
    private AGDProperties agdProperties;

    @Autowired
    private YOMAProperties yomaProperties;

    @Autowired
    private MPUProperties mpuProperties;

    public String generateUABHashHmac(String stringData){
        return generateHashHmac(stringData,uabProperties.getSecretKey(),"HmacSHA1");
    }

    public String generateAGDHashHMac(String stringData){
        return generateHashHmac(stringData,agdProperties.getSecretKey(),"HmacSHA1");
    }

    public String generateCBHashSHA256(String stringData) throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        final byte[] hashbytes = digest.digest(
                stringData.getBytes(StandardCharsets.UTF_8));
        return bytesToHexLowerCase(hashbytes);
    }

    public String generateYOMAHashSHA256(String stringData){
        return generateHashHmacLowerCase(stringData,yomaProperties.getSecretKey(),"HmacSHA256");
    }

    private String generateMPUHashHmac(String stringData){
        return generateHashHmac(stringData,mpuProperties.getSecretKey(),"HmacSHA1");
    }

    public String generateHashHmac(String stringData, String secretKey, String hMac) {
        return bytesToHex(generateHashHmacByte(stringData,secretKey,hMac));
    }

    public String generateHashHmacLowerCase(String stringData, String secretKey, String hMac) {
        return bytesToHexLowerCase(generateHashHmacByte(stringData,secretKey,hMac));
    }

    public byte[] generateHashHmacByte(String stringData, String secretKey, String hMac) {
        Mac sha_HMAC = null;
        try {
            byte[] byteKey = secretKey.getBytes(StandardCharsets.UTF_8);
            sha_HMAC = Mac.getInstance(hMac);
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, hMac);
            sha_HMAC.init(keySpec);
            byte[] mac_data = sha_HMAC.
                    doFinal(stringData.getBytes(StandardCharsets.UTF_8));

            return mac_data;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public SecretKeySpec getSecretKeySpec(String secret) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        SecretKeySpec secretKey;
        byte[] key;

        MessageDigest sha = null;
        key = secret.getBytes("UTF-8");
        sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        secretKey = new SecretKeySpec(key, "AES");

        return secretKey;
    }

    public String encryptAES256(String strToEncrypt, String secret) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeyException {
        SecretKeySpec secretKey=getSecretKeySpec(secret);

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
    }

    public String decryptAES256(String strToDecrypt, String secret) throws NoSuchPaddingException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKeySpec secretKey=getSecretKeySpec(secret);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
    }

    public String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        return  bytesToHex(bytes,hexArray);
    }

    public String bytesToHexLowerCase(byte[] bytes){
        final char[] hexArray = "0123456789abcdef".toCharArray();
        return  bytesToHex(bytes,hexArray);
    }

    public String bytesToHex(byte[] bytes, char[] hexArray) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
