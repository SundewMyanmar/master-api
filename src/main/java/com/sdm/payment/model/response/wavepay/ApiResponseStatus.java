package com.sdm.payment.model.response.wavepay;

public enum ApiResponseStatus {
    SUCCESS("200"),
    ALREADY_EXIST("409"),
    INVALID_HASH("400"),
    NULL_OR_INVALID_PAYLOAD("422"),
    INVALID_MERCHANT("404");

    private String value;

    ApiResponseStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}