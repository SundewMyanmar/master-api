package com.sdm.core.db;

import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Filterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DefaultRepositoryImpl<T extends DefaultEntity, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements DefaultRepository<T, ID> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRepositoryImpl.class);

    private final JpaEntityInformation<T, ?> entityInformation;
    private final EntityManager entityManager;
    private final List<String> filterableFields;
    private static final String DELETED_AT = "deletedAt";

    private final List<String> getFilterableFields(Class<T> entityClass) {
        List<String> fields = new ArrayList();
        Arrays.stream(entityClass.getDeclaredFields()).forEach(field ->
                Arrays.stream(field.getDeclaredAnnotations()).forEach(annotation -> {
                    if (annotation.annotationType().equals(Filterable.class)) {
                        fields.add(field.getName());
                    }
                })
        );
        return fields;
    }

    public DefaultRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityInformation = entityInformation;
        this.entityManager = entityManager;
        this.filterableFields = getFilterableFields(entityInformation.getJavaType());
    }

    @Override
    public Page<T> findAll(Pageable pageable, String filter) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<T> cQuery = builder.createQuery(this.getDomainClass());
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);

        Root<T> root = cQuery.from(getDomainClass());
        Root<T> countRoot = countQuery.from(getDomainClass());

        //Filter Predicates
        final List<Predicate> predicates = new ArrayList<>();
        final List<Predicate> countPredicates = new ArrayList<>();

        this.filterableFields.forEach(f -> {
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

    @Override
    public void softDeleteById(ID id) {
        Assert.notNull(id, "The given id must not be null!");

        Optional<T> entity = findById(id);

        if (!entity.isPresent())
            throw new EmptyResultDataAccessException(
                    String.format("No %s entity with id %s exists!", entityInformation.getJavaType(), id), 1);

        softDelete(entity.get());
    }

    @Override
    public void softDelete(Iterable<T> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");
        for (T entity : entities)
            softDelete(entity);
    }

    @Override
    public void softDelete(T entity) {
        Assert.notNull(entity, "The entity must not be null!");

        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();

        CriteriaUpdate<T> update = cb.createCriteriaUpdate(this.getDomainClass());

        Root<T> root = update.from(this.getDomainClass());

        update.set(DELETED_AT, LocalDateTime.now());

        final List<Predicate> predicates = new ArrayList<Predicate>();

        if (entityInformation.hasCompositeId()) {
            for (String s : entityInformation.getIdAttributeNames())
                predicates.add(cb.equal(root.<ID>get(s),
                        entityInformation.getCompositeIdAttributeValue(entityInformation.getId(entity), s)));
            update.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        } else
            update.where(cb.equal(root.<ID>get(entityInformation.getIdAttribute().getName()),
                    entityInformation.getId(entity)));

        this.entityManager.createQuery(update).executeUpdate();
    }
}
