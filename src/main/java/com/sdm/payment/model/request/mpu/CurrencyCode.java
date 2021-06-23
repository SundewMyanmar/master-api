package com.sdm.payment.model.request.mpu;

/**
 * Standard ISO4217
 * Currency Codes
 */
public enum CurrencyCode {
    MMK(104);

    Integer value;

    CurrencyCode(Integer value) {
        this.value = value;
    }

    public Long getValue() {
        return Long.valueOf(value);
    }
}