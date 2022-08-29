package com.sdm.core.service;

import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.response.PaginationResponse;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.LocaleManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Service
@Log4j2
public abstract class ReactiveService<T extends DefaultEntity, ID extends Serializable> {
    protected abstract DefaultRepository<T, ID> getRepository();

    @Autowired
    protected LocaleManager localeManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    @Qualifier("jdbcScheduler")
    public Scheduler jdbcScheduler;

    /*Reactive Flux*/
    public Mono<PaginationResponse<T>> findAll(String filter, Pageable pageable) {

        if (Globalizer.isNullOrEmpty(filter)) filter = "";

        Map<String, CriteriaQuery<?>> map = this.getRepository().findAllQuery(filter, pageable);
        CriteriaQuery<T> cQuery = (CriteriaQuery<T>) map.get("data-query");
        CriteriaQuery<Long> countQuery = (CriteriaQuery<Long>) map.get("count-query");

        //Get Result With Pageable
        TypedQuery<T> query = this.getRepository().getEntityManager().createQuery(cQuery);
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());

        Flux<T> result = Flux.defer(() -> Flux.fromIterable(
                query.getResultList()
        )).subscribeOn(jdbcScheduler);

        Mono<Long> count = Mono.defer(() -> Mono.just(
                this.getRepository().getEntityManager().createQuery(countQuery).getSingleResult()
        )).subscribeOn(jdbcScheduler);

        return result.collectList()
                .zipWith(count)
                .map(t -> new PaginationResponse(new PageImpl(t.getT1(), pageable, t.getT2())));
    }

    public Flux<T> findAll() {
        return Flux.defer(() -> Flux.fromIterable(
                this.getRepository().findAll()
        )).subscribeOn(jdbcScheduler);
    }

    public Mono<T> findById(ID id) {
        return Mono.defer(() -> Mono.just(
                        this.getRepository().findById(id).orElseThrow(
                                () -> new GeneralException(HttpStatus.NOT_ACCEPTABLE,
                                        localeManager.getMessage("no-data-by", id)))
                ))
                .subscribeOn(jdbcScheduler);
    }

    public Mono<T> save(T entity) {
        return Mono.fromCallable(
                () -> transactionTemplate.execute(status -> this.getRepository().save(entity))
        ).subscribeOn(jdbcScheduler);
    }
}
