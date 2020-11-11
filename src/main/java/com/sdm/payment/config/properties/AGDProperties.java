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
@ConfigurationProperties(prefix = "com.sdm.payment.agd")
public class AGDProperties {
    public enum APIResponseStatus{
        SUCCESS("000"),
        FIELD_REQUIRED("012"),
        SYSTEM_ERROR("014"),
        INVALID_WALLET_USER_ID("062"),
        LIMIT_COUNT_EXCEEDED("556"),
        INVALID_SEQUENCE_NO("558"),
        REVERSAL_SUCCESS("559"),
        INVALID_MERCHANT_CHANNEL("905"),
        DUPLICATE_SEQUENCE_NO("906"),
        INVALID_MERCHANT_ACCOUNT("907"),
        INVALID_SECURITY("060"),
        INVALID_VERSION("910"),
        INACTIVE_MERCHANT_USER("105"),
        NEED_UPDATE("106");

        private String value;

        APIResponseStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum TransactionStatus{
        SUCCESS("000"),
        CANCEL("012"),
        TIMEOUT("013"),
        SYSTEM_ERROR("014");

        private String value;

        TransactionStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private String url = "http://www.sundewmyanmar.com/";
    private String user = "info@sundewmyanmar.com";
    private String secretKey= "X3h0#^@ink";
    private String channel="SUNDEW_MYANMAR";
    private String version="02";

    @Autowired
    private PaymentProperties paymentProperties;

    public String getVerifyPhoneUrl(){return paymentProperties.replaceUrl(url+"Ver01/Wallet/Wallet_VerifyPhoneNumber");}

    public String getDirectPaymentUrl(){return paymentProperties.replaceUrl(url+"Ver02/Wallet/Wallet_DirectAPIV2");}

    public String getCheckTransactionUrl(){return paymentProperties.replaceUrl(url+"Ver02/Wallet/Wallet_CheckTransactionStatus");}

    public String getDirectPaymentCallbackUrl(){
        return paymentProperties.replaceUrl(paymentProperties.getCallbackUrl()+"agd/payments/public/direct/callback");
    }
}
