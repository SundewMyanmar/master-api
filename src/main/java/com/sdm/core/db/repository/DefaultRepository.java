package com.sdm.core.db.repository;

import com.sdm.core.model.AdvancedFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.NoRepositoryBean;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@NoRepositoryBean
public interface DefaultRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    Page<T> findAll(String filter, Pageable pageable);

    Page<T> advancedSearch(List<AdvancedFilter> search, Pageable pageable);

    @Modifying
    void softDeleteById(ID id);

    @Modifying
    void softDelete(T entity);

    @Modifying
    void softDelete(Iterable<T> entities);

    EntityManager getEntityManager();

    public Map<String, CriteriaQuery<?>> findAllQuery(String filter, Pageable pageable);
}
