package com.sdm.payment.service;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

@Log4j2
public abstract class BasePaymentService {
    public abstract String getPayment();

    public enum LogType {
        INFO,
        ERROR,
        REQUEST,
        RESPONSE,
        CALLBACK_REQUEST,
        CALLBACK_RESPONSE
    }

    protected void writeLog(LogType type, String message) {
        Marker marker = MarkerManager.getMarker(this.getPayment().toUpperCase());
        log.info(marker, "{} => {}", type, message);
    }
}
