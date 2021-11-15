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
@ConfigurationProperties(prefix = "com.sdm.payment.agd")
public class OnePayProperties {
    private String url = "https://onepay.mobi/API/";
    private String user = "";
    private String secretKey = "";
    private String channel = "";
    private String version = "02";
    private Integer expSeconds = 300;

    public String getPhoneNo(String ph) {
        if (ph == null) return null;
        return "959" + ph;
    }

    public String getVerifyPhoneUrl() {
        return (url + "Ver01/Wallet/Wallet_VerifyPhoneNumber");
    }

    public String getDirectPaymentUrl() {
        return (url + "Ver02/Wallet/Wallet_DirectAPIV2");
    }

    public String getCheckTransactionUrl() {
        return (url + "Ver02/Wallet/Wallet_CheckTransactionStatus");
    }

    public String getPaymentCallbackUrl() {
        log.info("ONEPay CALLBACK=> " + Globalizer.getCurrentContextPath("/public/payments/onepay/callback", true));
        return Globalizer.getCurrentContextPath("/public/payments/onepay/callback", true);
    }
}
