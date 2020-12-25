package com.sdm.payment.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "com.sdm.payment.yoma")
public class YOMAProperties {
    private String url = "";
    private String merchantId = "";
    private String merchantName = "";
    private String secretKey = "";
    private Integer timeToLiveInSeconds = 300;

    @Autowired
    private PaymentProperties paymentProperties;

    public String getFrontEndResultUrl() {
        //TODO: frontEndResultUrl if available in the future
        return paymentProperties.getCallbackUrl();
    }

    public String getPaymentRequestUrl() {
        return paymentProperties.replaceUrl(url + "payment");
    }

    public String getPaymentRequestAuthenticateUrl(String transactionId) {
        return paymentProperties.replaceUrl(url + "authenticate?transaction_id=" + transactionId);
    }

    public String getPaymentCallbackUrl() {
        return paymentProperties.replaceUrl(paymentProperties.getCallbackUrl() + "public/payments/wave/callback");
    }
}
