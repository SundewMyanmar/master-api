package com.sdm.payment.model.request.mpu;

public enum CurrencyCode {
    MYANMAR_KYAT(104);

    Integer value;

    CurrencyCode(Integer value) {
        this.value = value;
    }

    public Long getValue() {
        return Long.valueOf(value);
    }
}