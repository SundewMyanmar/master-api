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
@ConfigurationProperties(prefix = "com.sdm.payment.mpu")
public class MPUProperties {
    private String url;

    private String merchantId;

    private String secretKey;

    private String version;

    @Autowired
    private PaymentProperties paymentProperties;

    public String getPaymentRequestUrl() {
        return paymentProperties.replaceUrl(url + "Payment/Payment/pay");
    }

    //Inquiry = I, Voice = V
    public String getPaymentInquiryUrl() {
        return paymentProperties.replaceUrl(url + "Payment/Action/api");
    }

    public String getDirectPaymentCallbackUrl() {
        return paymentProperties.replaceUrl(paymentProperties.getCallbackUrl() + "mpu/payments/public/direct/callback");
    }
}
