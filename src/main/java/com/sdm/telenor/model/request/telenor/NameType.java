package com.sdm.telenor.model.request.telenor;

public enum NameType {
    UNKNOWN("0"),
    INTERNATIONAL("1"),
    NATIONAL("2"),
    NETWORK_SPECIFIC("3"),
    SUBSCRIBER_NUMBER("4"),
    ALPHANUMERIC("5"),
    ABBREVIATED("6");

    private String value;

    NameType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
