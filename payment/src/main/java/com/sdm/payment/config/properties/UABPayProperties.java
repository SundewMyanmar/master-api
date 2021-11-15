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
public class UABPayProperties {
    private String url = "https://www.uabpaybusiness.com/API/";
    private String user = "";
    private String password = "";
    private String secretKey = "";
    private String channel = "";
    private String appName = "uabpay";
    private Integer expSeconds = 300;

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
        log.info("UABPay CALLBACK=> " + Globalizer.getCurrentContextPath("/public/payments/uab/callback", true));
        return Globalizer.getCurrentContextPath("/public/payments/uab/callback", true);
    }

    public String getCheckTransactionStatus() {
        return (url + "Ver01/Wallet/Wallet_CheckTransactionStatus");
    }
}
