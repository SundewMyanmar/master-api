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
@ConfigurationProperties(prefix = "com.sdm.payment.cb")
public class CBProperties {
    private String url = "http://www.sundewmyanmar.com/";
    private String authToken = "";
    private String ecommerceId = "";
    private String subMerId = "";
    private String currency = "MMK";

    @Autowired
    private PaymentProperties paymentProperties;

    public String getPaymentOrderUrl() {
        return paymentProperties.replaceUrl(url + "v1/request-payment-order.service");
    }

    public String getCheckPaymentStatusUrl() {
        return paymentProperties.replaceUrl(url + "v1/checkstatus-webpayment.service");
    }

    public String getNotifyUrl() {
        return paymentProperties.replaceUrl(paymentProperties.getCallbackUrl() + "public/payments/cb/callback");
    }

    public String getDeepLinkUrl(String id) {
        return "cbuat://pay?keyreference=" + id;
    }
}
