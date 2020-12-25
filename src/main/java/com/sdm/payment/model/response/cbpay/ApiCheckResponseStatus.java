package com.sdm.payment.model.response.cbpay;

public enum ApiCheckResponseStatus {
    SUCCESS("0000"),
    INVALID_GENERATE_REF_ORDER("PNV0401"),
    INVALID_ECOMMERCE_ID("PNV0402"),
    INVALID_ORDER_ID("PNV0403"),
    AUTHEN_TOKEN_NOT_FOUND("PNV04201"),
    APP_SHOP_ID_NOT_FOUND("PNV04202"),
    ORDER_ID_NOT_FOUND("PNV04203");

    private String value;

    ApiCheckResponseStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}