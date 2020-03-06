package com.sdm.admin.repository;

import com.sdm.admin.model.SystemRoute;
import com.sdm.core.db.DefaultRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemRouteRepository extends DefaultRepository<SystemRoute, Integer> {
    @Query("SELECT distinct p FROM admin.SystemRouteEntity p JOIN p.allowRoles r WHERE r.id = :roleId")
    Optional<List<SystemRoute>> findByRoleId(@Param("roleId") int roleId);

    @Query("SELECT p FROM admin.SystemRouteEntity p WHERE p.httpMethod=:httpMethod AND p.pattern=:pattern")
    Optional<SystemRoute> findOneByHttpMethodAndPattern(@Param("httpMethod") String method, @Param("pattern") String pattern);

    @Override
    @Query("SELECT p from admin.SystemRouteEntity p JOIN p.allowRoles r WHERE lower(concat(p.pattern,p.httpMethod,r.name)) LIKE lower(concat('%' , :filter, '%'))")
    Page<SystemRoute> findAll(@Param("filter") String filter, Pageable pageable);

    Optional<SystemRoute> findOneByOrderByIdDesc();
}












