package com.sdm.payment.model.response.onepay;

public enum TransactionStatus {
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