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
@SettingFile(value = "kbz-pay-config.json", icon = "credit_card")
public class KBZPayProperties implements Serializable {
    private String url = "https://api.kbzpay.com/payment/gateway/";
    private String version = "1.0";
    @Encrypt
    private String appId;
    @Encrypt
    private String merchantCode;
    private String merchantName;
    @Encrypt
    private String secretKey;
    private Boolean isUat;

    public String getPaymentCallbackUrl() {
        log.info("KBZPay CALLBACK => " + Globalizer.getCurrentContextPath("/public/payments/kbz/callback", true));
        return Globalizer.getCurrentContextPath("/public/payments/kbz/callback", true);
    }

    public String getPaymentOrderUrl() {
        return (url + "precreate");
    }

    public enum PaymentMethod {
        CREATE("kbz.payment.precreate"),
        QUERY("kbz.payment.queryorder"),
        REFUND("kbz.payment.refund");

        private String value;

        PaymentMethod(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
