package com.sdm.payment.config.properties;

import com.sdm.core.util.annotation.SettingFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
@AllArgsConstructor
@NoArgsConstructor
@SettingFile("aya-pay-config.json")
public class AYAPayProperties {
    private String url = "https://opensandbox.ayainnovation.com/";
    private String consumerKey = "";
    private String consumerSecret = "";
    private String decryptionKey = "";
    private String userId = "";
    private String phone = "";
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
