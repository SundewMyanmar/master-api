package com.sdm.payment.util;

import com.sdm.core.security.SecurityManager;
import com.sdm.payment.config.properties.AGDProperties;
import com.sdm.payment.config.properties.MPUProperties;
import com.sdm.payment.config.properties.UABProperties;
import com.sdm.payment.config.properties.YOMAProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    @Autowired
    private SecurityManager securityManager;

    public String generateUABHashHmac(String stringData) {
        String encryptData = securityManager.generateHashHmac(stringData, uabProperties.getSecretKey(), "HmacSHA1");
        return encryptData;
    }

    public String generateAGDHashHMac(String stringData) {
        String encryptData = securityManager.generateHashHmac(stringData, agdProperties.getSecretKey(), "HmacSHA1");
        return encryptData;
    }

    public String generateYOMAHashSHA256(String stringData) {
        String encryptData = securityManager.generateHashHmac(stringData, yomaProperties.getSecretKey(), "HmacSHA256");
        return encryptData.toLowerCase();
    }

    public String generateMPUHashHmac(String stringData) {
        String encryptData = securityManager.generateHashHmac(stringData, yomaProperties.getSecretKey(), "HmacSHA1");
        return encryptData;
    }

    public String generateCBHashSHA256(String stringData) throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        final byte[] hashbytes = digest.digest(
                stringData.getBytes(StandardCharsets.UTF_8));
        return securityManager.bytesToHex(hashbytes).toLowerCase();
    }
}
