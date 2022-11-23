package com.sdm.core.config;

import com.sdm.core.Constants;
import com.sdm.core.db.repository.DefaultRepositoryImpl;
import com.sdm.core.model.Auditor;
import com.sdm.core.model.AuthInfo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.concurrent.Executors;

import javax.persistence.EntityManagerFactory;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableJpaRepositories(basePackages = "com.sdm", repositoryBaseClass = DefaultRepositoryImpl.class)
@EnableTransactionManagement
public class JpaConfig {

    @Value("${spring.datasource.maximum-pool-size:100}")
    private int connectionPoolSize;

    @Bean
    public Scheduler jdbcScheduler() {
        return Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize));
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    public AuditorAware<Auditor> auditorProvider() {
        return () -> {
            Auditor auditor = new Auditor(0, Constants.Auth.DEFAULT_AUTH_TOKEN);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof AuthInfo) {
                AuthInfo authInfo = (AuthInfo) authentication.getPrincipal();
                if (authInfo != null) {
                    auditor.setId(authInfo.getUserId());
                    auditor.setToken(authInfo.getToken());
                }
            }
            return Optional.of(auditor);
        };
    }
}