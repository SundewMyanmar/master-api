package com.sdm.core.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.Constants;
import com.sdm.core.model.Auditor;
import com.sdm.core.model.AuthInfo;
import com.sdm.core.model.SundewAuditEntity;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.hibernate.envers.RevisionListener;
import org.hibernate.envers.boot.internal.EnversService;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.*;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;

@Component
@Log4j2
public class HibernateAuditListener implements RevisionListener, PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private ObjectMapper objectMapper;

    private void writeLog(String type, String entityName, String message) {
        Marker marker = MarkerManager.getMarker(type.toUpperCase());
        log.info(marker, String.format("%s => %s", entityName, message));
    }

    @PostConstruct
    protected void init() {
        SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
        EnversService enversService = sessionFactory.getServiceRegistry().getService(EnversService.class);
        registry.getEventListenerGroup(EventType.POST_INSERT).appendListener(this);
        registry.getEventListenerGroup(EventType.POST_UPDATE).appendListener(this);
        registry.getEventListenerGroup(EventType.POST_DELETE).appendListener(this);
    }

    @Override
    public void newRevision(Object revisionEntity) {
        try {
            SundewAuditEntity auditEntity = (SundewAuditEntity) revisionEntity;

            //Create Auditor Info
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Auditor auditor = new Auditor(0, Constants.Auth.DEFAULT_AUTH_TOKEN);
            if (authentication != null && authentication.getPrincipal() instanceof AuthInfo) {
                AuthInfo authInfo = (AuthInfo) authentication.getPrincipal();
                auditor = new Auditor(authInfo.getUserId(), authInfo.getToken());
            }
            auditEntity.setAuditor(auditor);
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage(), ex.getCause());
        }
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        String className = event.getEntity().getClass().getName();
        try {
            String jsonString = objectMapper.writeValueAsString(event.getEntity());
            this.writeLog("INSERTED", className, jsonString);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        String className = event.getEntity().getClass().getName();
        try {
            String jsonString = objectMapper.writeValueAsString(event.getEntity());
            this.writeLog("MODIFIED", className, jsonString);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        String className = event.getEntity().getClass().getName();
        try {
            String jsonString = objectMapper.writeValueAsString(event.getEntity());
            this.writeLog("DELETED", className, jsonString);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }
}