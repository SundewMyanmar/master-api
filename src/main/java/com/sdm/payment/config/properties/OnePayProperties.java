package com.sdm.payment.config.properties;

import com.sdm.core.util.Globalizer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URL;

@Log4j2
@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "com.sdm.payment.agd")
public class OnePayProperties {
    private String url = "http://www.sundewmyanmar.com/";
    private String user = "info@sundewmyanmar.com";
    private String secretKey = "$3cR3tK3y";
    private String channel = "SUNDEW_MYANMAR";
    private String version = "02";

    public String getPhoneNo(String ph){
        if(ph==null)return null;
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
        log.info("ONEPay CALLBACK=> "+Globalizer.getCurrentContextPath("/public/payments/onepay/callback",true));
        return Globalizer.getCurrentContextPath("/public/payments/onepay/callback",true);
    }
}
