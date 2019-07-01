package com.sdm.core.repository;

import com.sdm.core.model.DefaultEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DefaultRepositoryImpl<T extends DefaultEntity, ID extends Serializable>
        extends SimpleJpaRepository<T, ID> implements DefaultRepository<T, ID> {
    private EntityManager entityManager;

    public DefaultRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    @Transactional
    public Page<T> findAll(Pageable pageable, String filter, List<String> entityFields) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<T> cQuery = builder.createQuery(getDomainClass());
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);

        Root<T> root = cQuery.from(getDomainClass());
        Root<T> countRoot = countQuery.from(getDomainClass());

        //Filter Predicates
        final List<Predicate> predicates = new ArrayList<>();
        final List<Predicate> countPredicates = new ArrayList<>();

        entityFields.forEach(f -> {
            predicates.add(builder.like(builder.lower(root.<String>get(f)), filter.toLowerCase() + "%"));
            countPredicates.add(builder.like(builder.lower(countRoot.<String>get(f)), filter.toLowerCase() + "%"));
        });

        //Select Statement
        cQuery.select(root);
        if (predicates.size() > 0)
            cQuery.where(builder.or(predicates.toArray(new Predicate[predicates.size()])));
        cQuery.orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

        //Get Total
        countQuery.select(builder.count(countRoot));
        if (countPredicates.size() > 0)
            countQuery.where(builder.or(countPredicates.toArray(new Predicate[countPredicates.size()])));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        //Get Result With Pageable
        TypedQuery<T> query = entityManager.createQuery(cQuery);
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(query.getResultList(), pageable, total);
    }
}
