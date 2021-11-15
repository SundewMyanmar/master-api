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
@SettingFile("wave-money-config.json")
public class WavePayProperties {
    private String url = "https://payments.wavemoney.io/payment";
    private String merchantId = "";
    private String merchantName = "";
    private String secretKey = "";
    private Integer expSeconds = 300;
    private String successUrl;

    public String getFrontEndCallbackUrl() {
        //TODO: frontEndResultUrl if available in the future
        return Globalizer.getCurrentContextPath("/", true);
    }

    public String getPaymentRequestUrl() {
        return (url + "payment");
    }

    public String getPaymentRequestAuthenticateUrl(String transactionId) {
        transactionId = Globalizer.encodeUrl(transactionId);
        return (url + "authenticate?transaction_id=" + transactionId);
    }

    public String getPaymentCallbackUrl() {
        log.info("WAVEPay CALLBACK=> " + Globalizer.getCurrentContextPath("/public/payments/wave/callback", true));
        return Globalizer.getCurrentContextPath("/public/payments/wave/callback", true);
    }
}
