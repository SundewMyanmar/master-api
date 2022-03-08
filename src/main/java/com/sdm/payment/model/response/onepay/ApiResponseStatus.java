package com.sdm.payment.model.response.onepay;

public enum ApiResponseStatus {
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

    ApiResponseStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}