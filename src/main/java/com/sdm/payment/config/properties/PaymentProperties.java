package com.sdm.payment.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "com.sdm.payment")
public class PaymentProperties {
    private String callbackUrl="http://localhost:8080/";
    private Integer expiredSeconds=300;

    public String getCallbackUrl(){return callbackUrl;}

    public String replaceUrl(String rawUrl){
        return rawUrl.replace(" ","%20");
    }
}
