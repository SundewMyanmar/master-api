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
import java.util.ArrayList;
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

    public static class NativeProcedureQuery {
        private String procedure;

        private List<ProcedureParam> params;

        private EntityManager entityManager;

        public NativeProcedureQuery(String procedure, List<ProcedureParam> params, EntityManager entityManager) {
            this.procedure = procedure;
            this.params = params;
            this.entityManager = entityManager;
        }

        public String getProcedure() {
            return procedure;
        }

        public void setProcedure(String procedure) {
            this.procedure = procedure;
        }

        public List<ProcedureParam> getParams() {
            return params;
        }

        public void setParams(List<ProcedureParam> params) {
            this.params = params;
        }

        public EntityManager getEntityManager() {
            return entityManager;
        }

        public void setEntityManager(EntityManager entityManager) {
            this.entityManager = entityManager;
        }

        public void execute() {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery(procedure);
            this.setParameter(query);
            query.execute();
        }

        public Object getSingleResult() {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery(procedure);
            this.setParameter(query);
            return query.getSingleResult();
        }

        public Map<String, Object> getSingleResult(List<String> outParam) {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery(procedure);
            this.setParameter(query);
            query.execute();
            Map<String, Object> resultMap = new HashMap<>();
            outParam.forEach(param -> resultMap.put(param, query.getOutputParameterValue(param)));

            return resultMap;
        }

        public List<?> getResultList() {
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

        private void setParameter(StoredProcedureQuery query) {
            if (this.params != null) {
                this.params.forEach(param -> {
                    query.registerStoredProcedureParameter(param.getName(), param.getpClass(), param.getParameterMode());
                    query.setParameter(param.getName(), param.getValue());
                });
            }
        }
    }

    public static class NativeQuery {
        private String queryString;
        private ResultLimit limit;
        private String filter;
        private List<String> filterFields;
        private Pageable pageable;
        private EntityManager entityManager;

        public NativeQuery() {
        }

        public NativeQuery(String queryString, EntityManager entityManager) {
            this.queryString = queryString;
            this.entityManager = entityManager;
            this.filterFields = new ArrayList<>();
        }

        public NativeQuery(String queryString, String filter, List<String> filterFields, EntityManager entityManager) {
            this.queryString = queryString;
            this.filter = filter;
            this.filterFields = filterFields;
            this.entityManager = entityManager;
        }

        public NativeQuery(String queryString, String filter, List<String> filterFields, ResultLimit limit, EntityManager entityManager) {
            this.queryString = queryString;
            this.filter = filter;
            this.filterFields = filterFields;
            this.limit = limit;
            this.entityManager = entityManager;
        }

        public NativeQuery(String queryString, String filter, List<String> filterFields, Pageable pageable, EntityManager entityManager) {
            this.queryString = queryString;
            this.filter = filter;
            this.filterFields = filterFields;
            this.pageable = pageable;
            this.entityManager = entityManager;
        }

        public String getQueryString() {
            return queryString;
        }

        public void setQueryString(String queryString) {
            this.queryString = queryString;
        }

        public ResultLimit getLimit() {
            return limit;
        }

        public void setLimit(ResultLimit limit) {
            this.limit = limit;
        }

        public String getFilter() {
            return filter;
        }

        public void setFilter(String filter) {
            this.filter = filter;
        }

        public List<String> getFilterFields() {
            return filterFields;
        }

        public void setFilterFields(List<String> filterFields) {
            this.filterFields = filterFields;
        }

        public Pageable getPageable() {
            return pageable;
        }

        public void setPageable(Pageable pageable) {
            this.pageable = pageable;
        }

        public EntityManager getEntityManager() {
            return entityManager;
        }

        public void setEntityManager(EntityManager entityManager) {
            this.entityManager = entityManager;
        }

        public Object getSingleResult(){
            Query query=this.getQuery(false);
            return query.getSingleResult();
        }

        public List<?> getResultList() {
            Query query = this.getQuery(true);

            if (this.limit != null) {
                query.setFirstResult(limit.getFirstResult());
                query.setMaxResults(limit.getMaxResult());
            }

            return query.getResultList();
        }

        public Page<?> getPaging() throws Exception {
            if (pageable == null) {
                throw new Exception("No Pageable Object Found!");
            }
            System.out.println("Hibernate Versions : " + org.hibernate.Version.getVersionString());

            String nativeQuery="SELECT * FROM " + this.queryString;
            if(this.queryString.toUpperCase().startsWith("SELECT")){
                nativeQuery=this.queryString;
            }
            Query query = entityManager.createNativeQuery(nativeQuery);
            //Check upcoming hibernate 6, to modify deprecated methods for future upgrade
            query.unwrap(org.hibernate.SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

            String nativeCountQuery="SELECT COUNT(*) FROM " + this.queryString;
            if(this.queryString.toUpperCase().startsWith("SELECT")){
                int index=queryString.toUpperCase().indexOf(" FROM ");
                nativeCountQuery="SELECT COUNT(*) "+this.queryString.substring(index,this.queryString.length());
            }
            Query countQuery = entityManager.createNativeQuery(nativeCountQuery);

            if (this.filter != null && !this.filter.isEmpty()) {
                for (int i = 0; i < this.filterFields.size(); i++) {
                    query.setParameter(this.filterFields.get(i), this.filter.toLowerCase() + "%");
                    countQuery.setParameter(this.filterFields.get(i), this.filter.toLowerCase() + "%");
                }
            }

            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());

            BigInteger total = (BigInteger) countQuery.getSingleResult();
            return new PageImpl<>(query.getResultList(), pageable, total.longValue());
        }

        private Query getQuery(boolean mapEntity) {
            String nativeQueryString = "SELECT * FROM " +this.queryString;
            if (this.queryString.toUpperCase().startsWith("SELECT")) {
                nativeQueryString =  this.queryString;
            }

            Query query = this.entityManager.createNativeQuery(nativeQueryString);
            //Check upcoming hibernate 6, to modify deprecated methods for future upgrade
            if(mapEntity)
                query.unwrap(org.hibernate.SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

            if (this.filter != null && !this.filter.isEmpty()) {
                for (int i = 0; i < this.filterFields.size(); i++) {
                    query.setParameter(this.filterFields.get(i), this.filter.toLowerCase() + "%");
                }
            }
            return query;
        }
    }

    private final EntityManager entityManager;

    public NativeQueryManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    //Native Procedure Query
    public NativeProcedureQuery procedureQuery(String procedure, List<ProcedureParam> params) {
        return new NativeProcedureQuery(procedure, params, entityManager);
    }

    private String createQueryString(String tableName, String filter, List<String> filterFields, Pageable pageable) {
        String queryString = tableName + " where 1=1";
        if (filter != null && !filter.isEmpty()) {
            for (int i = 0; i < filterFields.size(); i++) {
                if (i == 0) queryString += " AND";
                else queryString += " OR";
                queryString += " LOWER(" + filterFields.get(i) + ") LIKE :" + filterFields.get(i);
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

    public NativeQuery customQuery(String queryString){
        return new NativeQuery(queryString,entityManager);
    }

    public NativeQuery customQuery(String queryString, String filter, List<String> filterFields){
        return new NativeQuery(queryString, filter, filterFields,entityManager);
    }

    public NativeQuery customQuery(String queryString, String filter, List<String> filterFields, ResultLimit limit){
        return new NativeQuery(queryString, filter, filterFields, limit,entityManager);
    }

    public NativeQuery customQuery(String queryString, String filter, List<String> filterFields, Pageable pageable){
        return new NativeQuery(queryString, filter, filterFields, pageable,entityManager);
    }

    public NativeQuery query(String tableName, String filter, List<String> filterFields) {
        return new NativeQuery(this.createQueryString(tableName, filter, filterFields, null), filter, filterFields, entityManager);
    }

    public NativeQuery query(String tableName, String filter, List<String> filterFields, ResultLimit limit) {
        return new NativeQuery(this.createQueryString(tableName, filter, filterFields, null), filter, filterFields, limit, entityManager);
    }

    public NativeQuery query(String tableName, String filter, List<String> filterFields, Pageable pageable) {
        return new NativeQuery(this.createQueryString(tableName, filter, filterFields, pageable), filter, filterFields, pageable, entityManager);
    }
}
