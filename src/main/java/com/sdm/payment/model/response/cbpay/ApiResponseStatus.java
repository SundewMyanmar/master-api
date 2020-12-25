package com.sdm.payment.model.response.cbpay;

public enum ApiResponseStatus {
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

    ApiResponseStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}