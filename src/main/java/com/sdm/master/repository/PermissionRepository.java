package com.sdm.master.repository;

import com.sdm.core.security.PermissionMatcher;
import com.sdm.master.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, Integer> {
    @Query("SELECT p FROM PermissionEntity p WHERE p.role.id = :roleId")
    List<PermissionMatcher> findByRoleId(@Param("roleId") int roleId);
}
