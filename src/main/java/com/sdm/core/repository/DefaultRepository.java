package com.sdm.core.repository;

import com.sdm.core.model.DefaultEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;

@NoRepositoryBean
public interface DefaultRepository<T extends DefaultEntity, ID extends Serializable> extends JpaRepository<T, ID> {
    public Page<T> findAll(Pageable pageable, String filter, List<String> fields);
}
