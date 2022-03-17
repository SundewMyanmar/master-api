package com.sdm.payment.config.properties;

import com.sdm.core.util.Globalizer;
import com.sdm.core.util.annotation.SettingFile;
import com.sdm.core.util.annotation.Encrypt;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
@AllArgsConstructor
@NoArgsConstructor
@SettingFile(value="one-pay-config.json",icon="credit_card")
public class OnePayProperties {
    private String url = "https://onepay.mobi/API/";
    @Encrypt
    private String user = "";
    @Encrypt
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
