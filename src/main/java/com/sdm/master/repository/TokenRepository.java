package com.sdm.master.repository;

import com.sdm.master.entity.TokenEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<TokenEntity, String> {

    List<TokenEntity> findByUserId(long userId);

    Optional<TokenEntity> findByUserIdAndDeviceIdAndDeviceOs(long userId, String deviceId, String deviceOS);

    Optional<TokenEntity> findByDeviceIdAndDeviceOs(String deviceId, String deviceOS);

    @Query("SELECT t FROM TokenEntity t WHERE t.lastLogin BETWEEN :fromDate AND :toDate ORDER BY lastLogin")
    List<TokenEntity> findAllByInActiveTokens(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate, Pageable pageable);

    @Transactional
    @Modifying
    @Query("DELETE FROM TokenEntity t WHERE t.userId = :userId")
    void deleteInBulkByUserId(@Param("userId") long userId);
}
