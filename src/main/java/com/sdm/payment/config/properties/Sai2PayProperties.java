package com.sdm.payment.config.properties;

import com.sdm.core.util.Globalizer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "com.sdm.payment.uab")
public class Sai2PayProperties {
    private String url = "http://www.sundewmyanmar.com/";
    private String user = "info@sundewmyanmar.com";
    private String password = "b7tX.~A<J5";
    private String secretKey = "X3h0#^@ink";
    private String channel = "SUNDEW_MYANMAR";
    private String appName = "saisaipay";

    public String getPhoneNo(String ph) {
        if (ph == null) return null;
        return "09" + ph;
    }

    public String getLoginUrl() {
        return (url + "Ver01/Wallet/Wallet_Login");
    }

    public String getChangePasswordUrl() {
        return (url + "Ver01/Wallet/Wallet_ChangePassword");
    }

    public String getCheckPhoneUrl() {
        return (url + "Ver01/Wallet/Wallet_CheckPhoneNoAPI");
    }

    public String getEnquiryPaymentUrl() {
        return (url + "Ver01/Wallet/Wallet_PaymentAPI");
    }

    public String getEnquiryCallbackUrl() {
        log.info("Sai2Pay CALLBACK=> " + Globalizer.getCurrentContextPath("/public/payments/sai2/callback", true));
        return Globalizer.getCurrentContextPath("/public/payments/sai2/callback", true);
    }

    public String getCheckTransactionStatus() {
        return (url + "Ver01/Wallet/Wallet_CheckTransactionStatus");
    }
}
