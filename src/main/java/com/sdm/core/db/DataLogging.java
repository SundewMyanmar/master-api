package com.sdm.core.db;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

@Log4j2
public class DataLogging {

    private void writeLog(String type, String entityName, String message) {
        Marker marker = MarkerManager.getMarker(type.toUpperCase());
        log.info(marker, String.format("{} => {}", entityName, message));
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
