package com.sdm.admin.repository;

import com.sdm.admin.model.Role;
import com.sdm.core.Constants;
import com.sdm.core.db.repository.DefaultRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends DefaultRepository<Role, Integer> {
    @Query(value = "SELECT r.* FROM tbl_admin_roles r INNER JOIN tbl_admin_user_roles ur ON r.id = ur.role_id" +
            " AND ur.user_id = :userId and r.name <> '" + Constants.Auth.ROOT_ROLE + "'", nativeQuery = true)
    Optional<List<Role>> findByUserId(@Param("userId") int userId);

    @Query(value = "SELECT r.* FROM tbl_admin_roles r INNER JOIN tbl_admin_user_roles ur ON r.id = ur.role_id" +
            " AND ur.user_id = :userId and r.name <> '" + Constants.Auth.ROOT_ROLE + "' AND r.id IN (:roles)", nativeQuery = true)
    Optional<List<Role>> findRoleByUserIdAndRoleIds(@Param("userId") int userId, @Param("roles") Set<Integer> roles);

    List<Role> findByIdIn(Collection<Integer> ids);
}
