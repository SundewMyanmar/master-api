package com.sdm.payment.config.properties;

import com.sdm.core.util.Globalizer;
import com.sdm.core.util.annotation.Encrypt;
import com.sdm.core.util.annotation.SettingFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
@AllArgsConstructor
@NoArgsConstructor
@SettingFile(value = "mpu-config.json", icon = "credit_card")
public class MPUProperties {
    private String url = "https://www.mpu-ecommerce.com/";

    private String merchantId;

    @Encrypt
    private String secretKey;

    private String version = "2.2";

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
