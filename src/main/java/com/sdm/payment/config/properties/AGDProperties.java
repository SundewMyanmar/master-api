package com.sdm.payment.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "com.sdm.payment.agd")
public class AGDProperties {
    private String url = "http://www.sundewmyanmar.com/";
    private String user = "info@sundewmyanmar.com";
    private String secretKey = "X3h0#^@ink";
    private String channel = "SUNDEW_MYANMAR";
    private String version = "02";

    @Autowired
    private PaymentProperties paymentProperties;

    public String getVerifyPhoneUrl() {
        return paymentProperties.replaceUrl(url + "Ver01/Wallet/Wallet_VerifyPhoneNumber");
    }

    public String getDirectPaymentUrl() {
        return paymentProperties.replaceUrl(url + "Ver02/Wallet/Wallet_DirectAPIV2");
    }

    public String getCheckTransactionUrl() {
        return paymentProperties.replaceUrl(url + "Ver02/Wallet/Wallet_CheckTransactionStatus");
    }

    public String getDirectPaymentCallbackUrl() {
        return paymentProperties.replaceUrl(paymentProperties.getCallbackUrl() + "public/payments/onepay/callback");
    }
}
