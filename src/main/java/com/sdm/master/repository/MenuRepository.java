package com.sdm.master.repository;

import com.sdm.master.entity.MenuEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<MenuEntity, Integer> {
    @Query("SELECT distinct r from MenuEntity r JOIN r.roles ro  WHERE lower(concat(r.name,r.description,r.state,r.type,ro.name)) LIKE %:filter%")
    Page<MenuEntity> findByFilter(@Param("filter") String filter, Pageable pageable);

    @Query("SELECT r from MenuEntity r JOIN r.roles ro  WHERE ro.id in :ids")
    List<MenuEntity> findByRoles(@Param("ids") Integer[] ids);
}
