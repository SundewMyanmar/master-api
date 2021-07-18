package com.sdm.auth.model;

import com.sdm.core.db.repository.DefaultRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MultiFactorAuthRepository extends DefaultRepository<MultiFactorAuth, String> {
    Optional<MultiFactorAuth> findOneByUserIdAndMainTrue(int userId);

    Optional<MultiFactorAuth> findOneByUserIdAndKey(int userId, String key);

    @Query(value = "SELECT mfa FROM #{#entityName} mfa WHERE mfa.type = 'APP' AND mfa.userId = :userId")
    Optional<MultiFactorAuth> findAppByUserId(@Param("userId") int userId);

    long countByUserId(int userId);

    void deleteByUserId(int userId);

    @Modifying
    @Query(value = "UPDATE #{#entityName} mfa SET mfa.main = false WHERE mfa.userId = :userId")
    void clearMainMfa(@Param("userId") int userId);
}
