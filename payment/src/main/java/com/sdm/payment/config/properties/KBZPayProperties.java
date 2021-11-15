package com.sdm.payment.config.properties;

import com.sdm.core.util.Globalizer;
import com.sdm.core.util.annotation.SettingFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
@AllArgsConstructor
@NoArgsConstructor
@SettingFile("kbz-pay-config.json")
public class KBZPayProperties {
    private String url = "https://api.kbzpay.com/payment/gateway/";
    private String version = "1.0";
    private String appId;
    private String merchantCode;
    private String merchantName;
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
