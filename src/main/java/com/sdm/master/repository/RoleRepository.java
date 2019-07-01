package com.sdm.master.repository;

import com.sdm.Constants;
import com.sdm.core.repository.DefaultRepository;
import com.sdm.master.entity.RoleEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends DefaultRepository<RoleEntity, Integer> {

    @Query(value = "SELECT r.* FROM tbl_roles r INNER JOIN tbl_user_roles ur ON r.id = ur.role_id" +
        " AND ur.user_id = :userId and r.name <> '" + Constants.Auth.ROOT_ROLE + "'", nativeQuery = true)
    Optional<List<RoleEntity>> findByUserId(@Param("userId") long userId);
}
