package com.sdm.payment.config.properties;

import com.sdm.core.util.Globalizer;
import com.sdm.core.util.annotation.Encrypt;
import com.sdm.core.util.annotation.SettingFile;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
@AllArgsConstructor
@NoArgsConstructor
@SettingFile(value = "uab-pay-config.json", icon = "credit_card")
public class UABPayProperties implements Serializable {
    private String url = "https://www.uabpaybusiness.com/API/";
    @Encrypt
    private String user = "";
    @Encrypt
    private String password = "";
    @Encrypt
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
