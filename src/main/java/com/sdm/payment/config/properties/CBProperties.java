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
    public enum APIPaymentResponseStatus{
        SUCCESS("0000"),
        INVALID_AUTH_TOKEN("PNV0101"),
        INVALID_ECOMMERCE_ID("PNV0102"),
        INVALID_TRANSACTION_TYPE("PNV0103"),
        INACTIVE_STATUS("PNV0104"),
        GENERATE_FAIL("PNV0105"),
        AUTH_TOKEN_NOT_FOUND("PNV01201"),
        APP_SHOP_ID_NOT_FOUND("PNV01202"),
        TRANSACTION_TYPE_NOT_FOUND("PNV01203"),
        ORDER_ID_NOT_FOUND("PNV01204"),
        AMOUNT_NOT_FOUND("PNV01205"),
        CURRENCY_NOT_FOUND("PNV01206");

        private String value;

        APIPaymentResponseStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum APICheckResponseStatus{
        SUCCESS("0000"),
        INVALID_GENERATE_REF_ORDER("PNV0401"),
        INVALID_ECOMMERCE_ID("PNV0402"),
        INVALID_ORDER_ID("PNV0403"),
        AUTHEN_TOKEN_NOT_FOUND("PNV04201"),
        APP_SHOP_ID_NOT_FOUND("PNV04202"),
        ORDER_ID_NOT_FOUND("PNV04203");

        private String value;

        APICheckResponseStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private String url = "http://www.sundewmyanmar.com/";
    private String authToken="";
    private String ecommerceId="";
    private String subMerId="";
    private String currency="MMK";

    @Autowired
    private PaymentProperties paymentProperties;

    public String getPaymentOrderUrl(){
        return paymentProperties.replaceUrl(url+"v1/request-payment-order.service");
    }

    public String getCheckPaymentStatusUrl(){
        return paymentProperties.replaceUrl(url+"v1/checkstatus-webpayment.service");
    }

    public String getNotifyUrl(){
        return paymentProperties.replaceUrl(paymentProperties.getCallbackUrl()+"cb/payments/public/order/callback");
    }

    public String getDeepLinkUrl(String id){
        return "cbuat://pay?keyreference="+id;
    }
}
