package com.sdm.admin.repository;

import com.sdm.admin.model.SystemRoute;
import com.sdm.core.db.DefaultRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemRouteRepository extends DefaultRepository<SystemRoute, Integer> {

    @Query("SELECT p FROM admin.SystemRouteEntity p JOIN p.allowRoles r WHERE r.id = :roleId")
    Optional<List<SystemRoute>> findByRoleId(@Param("roleId") int roleId);

    @Query("SELECT p FROM admin.SystemRouteEntity p WHERE lower(p.httpMethod) = lower(:httpMethod) AND :pattern like p.pattern")
    Optional<List<SystemRoute>> checkPermissionRequest(@Param("httpMethod") String method, @Param("pattern") String pattern);

    Optional<SystemRoute> findOneByHttpMethodAndPattern(String method, String pattern);

    @Modifying
    @Query(value = "DELETE FROM tbl_admin_system_route_permissions p WHERE p.role_id = :roleId", nativeQuery = true)
    void clearPermissionByRoleId(@Param("roleId") int roleId);

    @Override
    @Query("SELECT p from admin.SystemRouteEntity p JOIN p.allowRoles r WHERE lower(concat(p.pattern,p.httpMethod,r.name)) LIKE lower(concat(:filter, '%'))")
    Page<SystemRoute> findAll(@Param("filter") String filter, Pageable pageable);
}












