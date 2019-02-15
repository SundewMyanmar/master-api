package com.sdm.master.repository;

import com.sdm.master.entity.TokenEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<TokenEntity, String> {

    Optional<List<TokenEntity>> findByUserId(long userId);

    Optional<TokenEntity> findByUserIdAndDeviceIdAndDeviceOs(long userId, String deviceId, String deviceOS);

    Optional<TokenEntity> findByDeviceIdAndDeviceOs(String deviceId, String deviceOS);

    Page<TokenEntity> findByLastLoginBetween(Date fromDate, Date toDate, Pageable pageable);

}
