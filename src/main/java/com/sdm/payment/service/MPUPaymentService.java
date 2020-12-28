package com.sdm.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.core.security.AESManager;
import com.sdm.payment.config.properties.MPUProperties;
import com.sdm.payment.model.request.mpu.CurrencyCode;
import com.sdm.payment.model.request.mpu.MPUPaymentRequest;
import com.sdm.payment.model.response.mpu.MPUPaymentResponse;
import com.sdm.payment.util.PaymentSecurityManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Service
@Log4j2
public class MPUPaymentService {
    @Autowired
    private PaymentSecurityManager securityManager;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MPUProperties mpuProperties;

    @Autowired
    private AESManager aesManager;
    
    @Autowired
    private ObjectMapper objectMapper;

    public MPUPaymentResponse requestPayment(String invoiceNo, String productDesc, Long amount, CurrencyCode currencyCode, String categoryCode,
                                             String userDefined1, String userDefined2, String userDefined3, String cardInfo) throws IOException, NoSuchAlgorithmException, KeyManagementException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException {
        String encryptCardInfo = aesManager.encrypt(cardInfo, mpuProperties.getSecretKey());
        MPUPaymentRequest request = new MPUPaymentRequest(mpuProperties.getVersion(), mpuProperties.getMerchantId(), invoiceNo, productDesc, amount, currencyCode.getValue(), categoryCode,
                userDefined1, userDefined2, userDefined3, encryptCardInfo, mpuProperties.getDirectPaymentCallbackUrl(), mpuProperties.getDirectPaymentCallbackUrl(), "");
        request.setHashValue(securityManager.generateMPUHashHmac(request.getSignatureString()));
        String rawUrl = mpuProperties.getPaymentRequestUrl();

        String resultString = paymentService.postRequest(new URL(rawUrl), objectMapper.writeValueAsString(request), null, true);
        MPUPaymentResponse result = objectMapper.readValue(resultString, MPUPaymentResponse.class);

        return result;
    }
}
