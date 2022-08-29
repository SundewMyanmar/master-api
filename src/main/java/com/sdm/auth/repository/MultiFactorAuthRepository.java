package com.sdm.auth.repository;

import com.sdm.auth.model.MultiFactorAuth;
import com.sdm.core.db.repository.DefaultRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MultiFactorAuthRepository extends DefaultRepository<MultiFactorAuth, String> {
    Optional<MultiFactorAuth> findOneByUserIdAndMfaKey(int userId, String key);

    Optional<MultiFactorAuth> findOneByUserIdAndMainTrueAndVerifyTrue(int userId);

    Optional<MultiFactorAuth> findOneByUserIdAndMfaKeyAndVerifyTrue(int userId, String key);

    Page<MultiFactorAuth> findByUserIdAndVerifyTrue(int userId, Pageable pageable);

    @Query(value = "SELECT mfa FROM #{#entityName} mfa WHERE mfa.type = 'APP' AND mfa.userId = :userId")
    Optional<MultiFactorAuth> findAppByUserId(@Param("userId") int userId);

    @Query(value = "SELECT mfa FROM #{#entityName} mfa WHERE mfa.type = 'APP' AND mfa.verify = true AND mfa.userId = :userId")
    Optional<MultiFactorAuth> findAppByUserIdAndVerifyTrue(@Param("userId") int userId);

    @Modifying
    @Query(value = "UPDATE #{#entityName} mfa SET mfa.verify = false WHERE mfa.userId = :userId")
    void disableAllMfa(@Param("userId") int userId);

    @Modifying
    @Query(value = "UPDATE #{#entityName} mfa SET mfa.main = false WHERE mfa.userId = :userId")
    void clearMainMfa(@Param("userId") int userId);
}
