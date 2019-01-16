package com.sdm.core.component;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

public class JpaAuditListener {
    private static final Logger logger = LoggerFactory.getLogger(JpaAuditListener.class);

    private void writeLog(String type, String entityName, String message) {
        Marker marker = MarkerFactory.getMarker(type.toUpperCase());
        logger.info(marker, "{} => {}", entityName, message);
    }

    @PostPersist
    public void postInsert(Object entity) {
        String className = entity.getClass().getName();
        JSONObject json = new JSONObject(entity);
        this.writeLog("INSERTED", className, json.toString());
    }


    @PostUpdate
    public void postUpdate(Object entity) {
        String className = entity.getClass().getName();
        JSONObject json = new JSONObject(entity);
        this.writeLog("MODIFIED", className, json.toString());
    }

    @PostRemove
    public void postRemove(Object entity) {
        String className = entity.getClass().getName();
        JSONObject json = new JSONObject(entity);
        this.writeLog("REMOVED", className, json.toString());
    }

}
