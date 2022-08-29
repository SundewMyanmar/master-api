package com.sdm.admin.repository;

import com.sdm.admin.model.SystemMenu;
import com.sdm.core.db.repository.DefaultRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemMenuRepository extends DefaultRepository<SystemMenu, Integer> {

    @Override
    @Query("SELECT distinct m from #{#entityName} m WHERE lower(concat(m.label,COALESCE(m.description, ''),COALESCE(m.path, ''))) LIKE lower(concat(:filter, '%'))")
    Page<SystemMenu> findAll(@Param("filter") String filter, Pageable pageable);

    @Query("SELECT distinct m from #{#entityName} m WHERE m.parentId IS NULL AND lower(concat(m.label,COALESCE(m.description, ''),COALESCE(m.path, ''))) LIKE lower(concat(:filter, '%')) ORDER BY m.priority")
    List<SystemMenu> findParentMenu(@Param("filter") String filter);

    @Query("SELECT distinct m from #{#entityName} m JOIN m.roles ro WHERE ro.id in :ids ORDER BY m.priority")
    List<SystemMenu> findByRoles(@Param("ids") List<Integer> ids);
}
