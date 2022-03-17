package com.sdm.payment.config.properties;

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
@SettingFile(value="aya-pay-config.json",icon="credit_card")
public class AYAPayProperties {
    private String url = "https://opensandbox.ayainnovation.com/";
    @Encrypt
    private String consumerKey = "";
    @Encrypt
    private String consumerSecret = "";
    @Encrypt
    private String decryptionKey = "";
    private String userId = "";
    @Encrypt
    private String phone = "";
    @Encrypt
    private String pin = "";
    private String currency = "MMK";

    public String getPaymentTokenUrl() {
        return url + "token";
    }

    public String getMerchantLogInUrl() {
        return url + "merchant/1.0.0/thirdparty/merchant/login";
    }

    public String getPaymentOrderUrl() {
        return url + "merchant/1.0.0/thirdparty/merchant/requestPushPayment";
    }

    public String getPhoneNo(String ph) {
        if (ph == null) return null;
        return "09" + ph;
    }
}
