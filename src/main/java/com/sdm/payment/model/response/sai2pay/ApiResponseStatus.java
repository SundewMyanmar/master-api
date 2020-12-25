package com.sdm.payment.model.response.sai2pay;

public enum ApiResponseStatus {
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

    ApiResponseStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}