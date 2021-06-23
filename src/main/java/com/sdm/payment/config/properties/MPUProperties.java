package com.sdm.payment.config.properties;

import com.sdm.core.util.Globalizer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Log4j2
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

    private String successUrl;

    public String getPaymentRequestUrl() {
        return (url + "Payment/Payment/pay");
    }

    //Inquiry = I, Void = V
    public String getPaymentInquiryUrl() {
        return (url + "Payment/Action/api");
    }

    public String getPaymentCallbackUrl() {
        log.info("MPU CALLBACK=> " + Globalizer.getCurrentContextPath("/public/payments/mpu/callback", true));
        return Globalizer.getCurrentContextPath("/public/payments/mpu/callback", true);
    }
}
