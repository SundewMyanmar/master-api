package com.sdm.core.db;

import com.sdm.Constants;
import com.sdm.core.model.AuthInfo;
import com.sdm.core.model.SundewAuditEntity;
import org.hibernate.envers.RevisionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class HibernateAuditListener implements RevisionListener {

    private static final Logger logger = LoggerFactory.getLogger(HibernateAuditListener.class);

    @Override
    public void newRevision(Object revisionEntity) {
        try {
            SundewAuditEntity auditEntity = (SundewAuditEntity) revisionEntity;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof AuthInfo) {
                AuthInfo authInfo = (AuthInfo) authentication.getPrincipal();
                auditEntity.setToken(authInfo.getToken());
                auditEntity.setUserId(authInfo.getUserId());
            } else {
                auditEntity.setUserId(0);
                auditEntity.setToken(Constants.Auth.DEFAULT_AUTH_TOKEN);
            }
        } catch (Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex.getCause());
        }
    }

}