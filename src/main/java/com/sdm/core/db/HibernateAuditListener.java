package com.sdm.core.db;

import com.sdm.Constants;
import com.sdm.core.model.AuthInfo;
import com.sdm.core.model.SundewAuditEntity;
import lombok.extern.log4j.Log4j2;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class HibernateAuditListener implements RevisionListener {

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
            log.error(ex.getLocalizedMessage(), ex.getCause());
        }
    }

}