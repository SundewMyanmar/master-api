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
@ConfigurationProperties(prefix = "com.sdm.payment.uab")
public class UABProperties {
    private String url = "http://www.sundewmyanmar.com/";
    private String user = "info@sundewmyanmar.com";
    private String password = "b7tX.~A<J5";
    private String secretKey = "X3h0#^@ink";
    private String channel = "SUNDEW_MYANMAR";
    private String appName = "saisaipay";

    @Autowired
    private PaymentProperties paymentProperties;

    public String getLoginUrl() {
        return paymentProperties.replaceUrl(url + "Ver01/Wallet/Wallet_Login");
    }

    public String getChangePasswordUrl() {
        return paymentProperties.replaceUrl(url + "Ver01/Wallet/Wallet_ChangePassword");
    }

    public String getCheckPhoneUrl() {
        return paymentProperties.replaceUrl(url + "Ver01/Wallet/Wallet_CheckPhoneNoAPI");
    }

    public String getEnquiryPaymentUrl() {
        return paymentProperties.replaceUrl(url + "Ver01/Wallet/Wallet_PaymentAPI");
    }

    public String getEnquiryCallbackUrl() {
        return paymentProperties.replaceUrl(paymentProperties.getCallbackUrl() + "public/payments/sai2/callback");
    }

    public String getCheckTransactionStatus() {
        return paymentProperties.replaceUrl(url + "Ver01/Wallet/Wallet_CheckTransactionStatus");
    }
}
