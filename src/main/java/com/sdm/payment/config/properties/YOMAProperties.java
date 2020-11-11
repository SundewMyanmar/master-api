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
    public enum APIPaymentResponseStatus{
        SUCCESS("200"),
        ALREADY_EXIST("409"),
        INVALID_HASH("400"),
        NULL_OR_INVALID_PAYLOAD("422"),
        INVALID_MERCHANT("404");

        private String value;

        APIPaymentResponseStatus(String value){this.value=value;}

        public String getValue(){return value;}
    }

    private String url="";
    private String merchantId="";
    private String merchantName="";
    private String secretKey="";
    private Integer timeToLiveInSeconds=300;

    @Autowired
    private PaymentProperties paymentProperties;

    public String getFrontEndResultUrl(){
        //TODO: frontEndResultUrl if available in the future
        return paymentProperties.getCallbackUrl();
    }

    public String getPaymentRequestUrl(){return paymentProperties.replaceUrl(url+"payment");}

    public String getPaymentRequestAuthenticateUrl(String transactionId){return paymentProperties.replaceUrl(url+"authenticate?transaction_id="+transactionId);}

    public String getPaymentCallbackUrl(){
        return paymentProperties.replaceUrl(paymentProperties.getCallbackUrl()+"yoma/payments/public/order/callback");
    }
}
