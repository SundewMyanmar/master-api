package com.sdm.core.db;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

public class DataLogging {
    private static final Logger logger = LoggerFactory.getLogger(DataLogging.class);

    private void writeLog(String type, String entityName, String message) {
        Marker marker = MarkerFactory.getMarker(type.toUpperCase());
        logger.info(marker, "{} => {}", entityName, message);
    }

    @PostPersist
    public void postInsert(Object entity) {
        String className = entity.getClass().getName();
        String jsonString = new Gson().toJson(entity);
        this.writeLog("INSERTED", className, jsonString);
    }


    @PostUpdate
    public void postUpdate(Object entity) {
        String className = entity.getClass().getName();
        String jsonString = new Gson().toJson(entity);
        this.writeLog("MODIFIED", className, jsonString);
    }

    @PostRemove
    public void postRemove(Object entity) {
        String className = entity.getClass().getName();
        String jsonString = new Gson().toJson(entity);
        this.writeLog("REMOVED", className, jsonString);
    }
}
