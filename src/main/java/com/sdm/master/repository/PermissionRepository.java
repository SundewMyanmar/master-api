package com.sdm.master.repository;

import com.sdm.core.security.PermissionMatcher;
import com.sdm.master.entity.PermissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, Integer> {
    @Query("SELECT p FROM PermissionEntity p JOIN p.roles r WHERE r.id = :roleId")
    Optional<List<PermissionMatcher>> findByRoleId(@Param("roleId") int roleId);

    @Query("SELECT p from PermissionEntity p JOIN p.roles r WHERE lower(concat(p.pattern,p.httpMethod,r.name)) LIKE %:filter%")
    Page<PermissionEntity> findByFilter(@Param("filter") String filter, Pageable pageable);

    Optional<PermissionEntity> findTopByOrderByIdDesc();
}
