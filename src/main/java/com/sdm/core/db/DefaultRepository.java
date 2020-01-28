package com.sdm.core.db;

import com.sdm.core.model.DefaultEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.NoRepositoryBean;

import javax.transaction.Transactional;
import java.io.Serializable;

@Transactional
@NoRepositoryBean
public interface DefaultRepository<T extends DefaultEntity, ID extends Serializable> extends JpaRepository<T, ID> {

    Page<T> findAll(Pageable pageable, String filter);

    @Modifying
    void softDeleteById(ID id);

    @Modifying
    void softDelete(T entity);

    @Modifying
    void softDelete(Iterable<T> entities);
}
