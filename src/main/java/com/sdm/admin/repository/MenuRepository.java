package com.sdm.admin.repository;

import com.sdm.admin.model.SystemMenu;
import com.sdm.core.db.DefaultRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends DefaultRepository<SystemMenu, Integer> {

    @Override
    @Query("SELECT distinct r from admin.SystemMenuEntity r JOIN r.roles ro  WHERE lower(concat(r.name,r.description,r.path,r.type,ro.name)) LIKE lower(concat('%', :filter, '%'))")
    Page<SystemMenu> findAll(String filter, Pageable pageable);

    @Query("SELECT distinct r from admin.SystemMenuEntity r JOIN r.roles ro  WHERE r.parentId IS NULL AND lower(concat(r.name,r.description,r.path,r.type,ro.name)) LIKE lower(concat('%', :filter, '%'))")
    List<SystemMenu> findParentMenu(String filter);

    @Query("SELECT r from admin.SystemMenuEntity r JOIN r.roles ro  WHERE ro.id in :ids")
    List<SystemMenu> findByRoles(@Param("ids") Integer[] ids);
}
