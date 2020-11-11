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
@ConfigurationProperties(prefix = "com.sdm.payment.uab")
public class UABProperties {
    public enum APIResponseStatus{
        SUCCESS("000"),
        FIELD_REQUIRED("012"),
        SYSTEM_ERROR("014"),
        INVALID_WALLET_USER_ID("062"),
        LIMIT_COUNT_EXCEEDED("778"),
        QR_EXPIRED("555"),
        INVALID_SEQUENCE_NO("777"),
        REVERSAL_SUCCESS("888"),
        TRANSCATION_FAIL("779"),
        INVALID_MERCHANT_CHANNEL("905"),
        DUPLICATE_SEQUENCE_NO("906"),
        INVALID_MERCHANT_ACCOUNT("907"),
        INVALID_SECURITY("060"),
        INVALID_VERSION("910"),
        INACTIVE_MERCHANT_USER("105");

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
    private String password = "b7tX.~A<J5";
    private String secretKey= "X3h0#^@ink";
    private String channel="SUNDEW_MYANMAR";
    private String appName="sundew";

    @Autowired
    private PaymentProperties paymentProperties;

    public String getLoginUrl(){
        return paymentProperties.replaceUrl(url+"Ver01/Wallet/Wallet_Login");
    }

    public String getChangePasswordUrl(){
        return paymentProperties.replaceUrl(url+"Ver01/Wallet/Wallet_ChangePassword");
    }

    public String getCheckPhoneUrl(){
        return paymentProperties.replaceUrl(url+"Ver01/Wallet/Wallet_CheckPhoneNoAPI");
    }

    public String getEnquiryPaymentUrl(){
        return paymentProperties.replaceUrl(url+"Ver01/Wallet/Wallet_PaymentAPI");
    }

    public String getEnquiryCallbackUrl(){
        return paymentProperties.replaceUrl(paymentProperties.getCallbackUrl()+"uab/payments/public/enquiry/callback");
    }

    public String getCheckTransactionStatus(){
        return paymentProperties.replaceUrl(url+"Ver01/Wallet/Wallet_CheckTransactionStatus");
    }
}
