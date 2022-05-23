package com.sdm.core.db.repository;

import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.AdvancedFilter;
import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Searchable;
import com.sdm.core.util.Globalizer;

import lombok.extern.log4j.Log4j2;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Log4j2
public class DefaultRepositoryImpl<T extends DefaultEntity, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements DefaultRepository<T, ID> {

    private static final String DELETED_AT = "deletedAt";
    private static final String VALID_FIELD_NAME = "^[a-z][a-zA-Z0-9]*(\\.id)?$";
    private static final String ENTITY_ALIAS = "e";
    private final JpaEntityInformation<T, ?> entityInformation;
    private final EntityManager entityManager;
    private final List<String> filterableFields;

    public DefaultRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityInformation = entityInformation;
        this.entityManager = entityManager;
        this.filterableFields = getSearchableFields(entityInformation.getJavaType());
    }

    private List<String> getSearchableFields(Class<T> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields()).filter(field -> field.isAnnotationPresent(Searchable.class) && field.getType() == String.class)
                .map((Field::getName)).collect(Collectors.toList());
    }

    protected boolean checkColumn(String column, Class<?> entity) {
        if (!Pattern.matches(VALID_FIELD_NAME, column)) {
            return false;
        }

        if (column.endsWith(".id")) {
            column = column.replaceAll(".id", "");
        }
        try {
            entity.getDeclaredField(column);
            return true;
        } catch (Exception ex) {
            if (entity.getSuperclass() != null) {
                return checkColumn(column, entity.getSuperclass());
            }
        }
        return false;
    }

    protected String getSortString(Pageable pageable) {
        Sort sort = pageable.getSort();
        List<String> orderList = new ArrayList<>();
        for (Sort.Order order : sort) {
            orderList.add(ENTITY_ALIAS + "." + order.getProperty() + " " + order.getDirection());
        }
        if (orderList.size() > 0) {
            return "ORDER BY " + String.join(", ", orderList);
        }
        return "";
    }

    @Override
    public Page<T> advancedSearch(List<AdvancedFilter> filters, Pageable pageable) {
        if (filters == null || filters.size() <= 0) {
            return super.findAll(pageable);
        }

        String query = "FROM " + this.getDomainClass().getName() + " " + ENTITY_ALIAS + " WHERE 1 = 1";
        Map<String, Object> params = new HashMap<>();

        try {
            //Add Condition
            for (AdvancedFilter filter : filters) {
                //Check Column for Security Reason
                if (!checkColumn(filter.getField(), this.getDomainClass())) {
                    throw new Exception("Invalid field name [" + filter.getField() + "].");
                }
                query += " AND (" + filter.getQuery(ENTITY_ALIAS, params) + ")";
            }

            String selectData = "SELECT " + ENTITY_ALIAS + " " + query;
            String selectCount = "SELECT COUNT(" + ENTITY_ALIAS + ") " + query;

            String orderBy = getSortString(pageable);
            if (orderBy.length() > 0) {
                selectData += " " + orderBy;
            }

            TypedQuery<T> dataQuery = entityManager.createQuery(selectData, this.getDomainClass());
            TypedQuery<Long> countQuery = entityManager.createQuery(selectCount, Long.class);

            for (String param : params.keySet()) {
                dataQuery.setParameter(param, params.get(param));
                countQuery.setParameter(param, params.get(param));
            }

            Long total = countQuery.getSingleResult();
            dataQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            dataQuery.setMaxResults(pageable.getPageSize());
            List<T> data = dataQuery.getResultList();

            return new PageImpl<T>(data, pageable, total);

        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
        }
    }

    @Override
    public Page<T> findAll(String filter, Pageable pageable) {
        if (Globalizer.isNullOrEmpty(filter)) {
            return super.findAll(pageable);
        }

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<T> cQuery = builder.createQuery(this.getDomainClass());
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);

        Root<T> root = cQuery.from(getDomainClass());
        Root<T> countRoot = countQuery.from(getDomainClass());

        //Filter Predicates
        final List<Predicate> predicates = new ArrayList<>();
        final List<Predicate> countPredicates = new ArrayList<>();
        final String likeFilter = "%" + filter.toLowerCase() + "%";

        this.filterableFields.forEach(f -> {
            predicates.add(builder.like(builder.lower(root.get(f)), likeFilter));
            countPredicates.add(builder.like(builder.lower(countRoot.get(f)), likeFilter));
        });

        //Select Statement
        cQuery.select(root);
        if (predicates.size() > 0)
            cQuery.where(builder.or(predicates.toArray(new Predicate[0])));
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

    @Transactional
    @Override
    public void softDeleteById(ID id) {
        Assert.notNull(id, "The given id must not be null!");

        Optional<T> entity = findById(id);

        if (!entity.isPresent())
            throw new EmptyResultDataAccessException(
                    String.format("No %s entity with id %s exists!", entityInformation.getJavaType(), id), 1);

        softDelete(entity.get());
    }

    @Transactional
    @Override
    public void softDelete(Iterable<T> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");
        for (T entity : entities)
            softDelete(entity);
    }

    @Transactional
    @Override
    public void softDelete(T entity) {
        Assert.notNull(entity, "The entity must not be null!");

        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();

        CriteriaUpdate<T> update = cb.createCriteriaUpdate(this.getDomainClass());

        Root<T> root = update.from(this.getDomainClass());

        update.set(DELETED_AT, new Date());

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
