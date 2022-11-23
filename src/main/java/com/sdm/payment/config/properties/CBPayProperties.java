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
@SettingFile(value = "cb-pay-config.json", icon = "credit_card")
public class CBPayProperties implements Serializable {
    private String url = "https://cbpay.cbbank.com.mm:10443/orderpayment-api/v1/";
    @Encrypt
    private String authToken = "";
    @Encrypt
    private String ecommerceId = "";
    @Encrypt
    private String subMerId = "";
    private String currency = "MMK";
    //cbuat, cb
    private String app = "cb";

    public String getPaymentOrderUrl() {
        return (url + "request-payment-order.service");
    }

    public String getCheckPaymentStatusUrl() {
        return (url + "checkstatus-webpayment.service");
    }

    public String getPaymentCallbackUrl() {
        log.info("CB CALLBACK => " + Globalizer.getCurrentContextPath("/public/payments/cb/callback", true));
        return Globalizer.getCurrentContextPath("/public/payments/cb/callback", true);
    }

    public String getDeepLinkUrl(String id) {
        return this.app + "://pay?keyreference=" + id;
    }
}
