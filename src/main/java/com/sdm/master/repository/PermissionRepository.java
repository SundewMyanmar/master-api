package com.sdm.master.repository;

import com.sdm.core.repository.DefaultRepository;
import com.sdm.core.security.PermissionMatcher;
import com.sdm.master.entity.PermissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends DefaultRepository<PermissionEntity, Integer> {
    @Query("SELECT distinct p FROM PermissionEntity p JOIN p.roles r WHERE r.id = :roleId")
    Optional<List<PermissionEntity>> findByRoleId(@Param("roleId") int roleId);

    @Query("SELECT p FROM PermissionEntity p WHERE p.httpMethod=:httpMethod AND p.pattern=:pattern")
    Optional<PermissionEntity> findByHttpMethodAndPattern(@Param("httpMethod") String method, @Param("pattern") String pattern);

    @Query("SELECT p from PermissionEntity p JOIN p.roles r WHERE lower(concat(p.pattern,p.httpMethod,r.name)) LIKE %:filter%")
    Page<PermissionEntity> findByFilter(@Param("filter") String filter, Pageable pageable);

    //@Query("SELECT P FROM PermissionEntity p ORDER BY id desc limit 1")
    Optional<PermissionEntity> findTopByOrderByIdDesc();
}












