package com.sdm.core.component;

import org.hibernate.transform.Transformers;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author m0n-hash
 */
@Component
@Scope("prototype")
public class NativeQueryManager {
    public static class ProcedureParam {
        private String name;
        private Object value;
        private Class<?> pClass;
        private ParameterMode parameterMode;

        public ProcedureParam(String name, Class<?> pClass, ParameterMode parameterMode, Object value) {
            this.name = name;
            this.value = value;
            this.pClass = pClass;
            this.parameterMode = parameterMode;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Class<?> getpClass() {
            return pClass;
        }

        public void setpClass(Class<?> pClass) {
            this.pClass = pClass;
        }

        public ParameterMode getParameterMode() {
            return parameterMode;
        }

        public void setParameterMode(ParameterMode parameterMode) {
            this.parameterMode = parameterMode;
        }
    }

    public static class ResultLimit {
        private int firstResult;
        private int maxResult;

        public ResultLimit(int maxResult) {
            this.firstResult = 0;
            this.maxResult = maxResult;
        }

        public ResultLimit(int firstResult, int maxResult) {
            this.firstResult = firstResult;
            this.maxResult = maxResult;
        }

        public int getFirstResult() {
            return firstResult;
        }

        public void setFirstResult(int firstResult) {
            this.firstResult = firstResult;
        }

        public int getMaxResult() {
            return maxResult;
        }

        public void setMaxResult(int maxResult) {
            this.maxResult = maxResult;
        }
    }

    private final EntityManager entityManager;

    public NativeQueryManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Query createNativeQuery(String query) {
        return entityManager.createNativeQuery(query);
    }

    public StoredProcedureQuery createStoreProcedure(String procedure, List<ProcedureParam> params) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(procedure);

        if (params != null) {
            params.forEach(param -> {
                query.registerStoredProcedureParameter(param.getName(), param.getpClass(), param.getParameterMode());
                query.setParameter(param.getName(), param.getValue());
            });
        }

        return query;
    }

    public void executeProcedure(String procedure, List<ProcedureParam> params) {
        this.createStoreProcedure(procedure, params).execute();
    }

    public Object executeProcedure_SingleResult(String procedure, List<ProcedureParam> params) {
        StoredProcedureQuery query = this.createStoreProcedure(procedure, params);
        return query.getSingleResult();
    }

    public Map<String, Object> executeProcedure_SingleResult(String procedure, List<ProcedureParam> params, List<String> outParam) {
        StoredProcedureQuery query = this.createStoreProcedure(procedure, params);
        query.execute();

        Map<String, Object> resultMap = new HashMap<>();
        outParam.forEach(param -> resultMap.put(param, query.getOutputParameterValue(param)));

        return resultMap;
    }

    public List<?> executeProcedure_ResultList(String procedure, List<ProcedureParam> params) {
        /*
        *TODO: To Fix Result List Map Without Native Query
        StoredProcedureQuery query = this.createStoreProcedure(procedure, params);
        query.unwrap(org.hibernate.SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        */
        String queryString = "call " + procedure + "(";
        if (params != null) {
            for (int i = 0; i < params.size(); i++) {
                if (i > 0) {
                    queryString += ",";
                }
                queryString += ":" + params.get(i).getName();
            }
        }

        Query query = entityManager.createNativeQuery(queryString + ")");
        query.unwrap(org.hibernate.SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

        if (params != null)
            params.forEach(param -> query.setParameter(param.getName(), param.getValue()));

        return query.getResultList();
    }

    private String createQueryString(String filter, List<String> filterFields, String tableName, Pageable pageable) {
        String queryString = tableName + " where 1=1";
        if (filter != null && !filter.isEmpty()) {
            for (int i = 0; i < filterFields.size(); i++) {
                if (i == 0) queryString += " AND";
                else queryString += " OR";
                queryString += " LOWER(" + filterFields.get(i) + ") LIKE :filter" + i;
            }
        }

        if (pageable != null) {
            Sort sorts = pageable.getSort();

            Long count = sorts.get().count();
            Sort.Order[] orders = sorts.get().toArray(o -> new Sort.Order[count.intValue()]);

            for (int i = 0; i < orders.length; i++) {
                if (i == 0) queryString += " ORDER BY ";
                else queryString += ", ";

                queryString += orders[i].getProperty();
                if (orders[i].getDirection().isAscending()) queryString += " ASC";
                else queryString += " DESC";
            }
        }
        return queryString;
    }

    private Query getNativeQuery(String filter, List<String> filterFields, String tableName) {
        String queryString = this.createQueryString(filter, filterFields, tableName, null);

        Query query = entityManager.createNativeQuery("SELECT * FROM " + queryString);
        //Check upcoming hibernate 6, to modify deprecated methods for future upgrade
        query.unwrap(org.hibernate.SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

        if (filter != null && !filter.isEmpty()) {
            for (int i = 0; i < filterFields.size(); i++) {
                query.setParameter("filter" + i, filter.toLowerCase() + "%");
            }
        }
        return query;
    }

    public List<?> findByNativeQuery(String filter, List<String> filterFields, String tableName) {
        Query query = this.getNativeQuery(filter, filterFields, tableName);

        return query.getResultList();
    }

    public List<?> findByNativeQuery(String filter, List<String> filterFields, String tableName, ResultLimit limit) {
        Query query = this.getNativeQuery(filter, filterFields, tableName);
        query.setFirstResult(limit.getFirstResult());
        query.setMaxResults(limit.getMaxResult());
        return query.getResultList();
    }

    public Page<?> findByNativeQuery(String filter, List<String> filterFields, String tableName, Pageable pageable) {
        System.out.println("Hibernate Versions : " + org.hibernate.Version.getVersionString());

        String queryString = this.createQueryString(filter, filterFields, tableName, pageable);

        Query query = entityManager.createNativeQuery("SELECT * FROM " + queryString);
        //Check upcoming hibernate 6, to modify deprecated methods for future upgrade
        query.unwrap(org.hibernate.SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

        Query countQuery = entityManager.createNativeQuery("SELECT COUNT(*) FROM " + queryString);

        if (filter != null && !filter.isEmpty()) {
            for (int i = 0; i < filterFields.size(); i++) {
                query.setParameter("filter" + i, filter.toLowerCase() + "%");
                countQuery.setParameter("filter" + i, filter.toLowerCase() + "%");
            }
        }

        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());

        BigInteger total = (BigInteger) countQuery.getSingleResult();
        return new PageImpl<>(query.getResultList(), pageable, total.longValue());
    }
}
