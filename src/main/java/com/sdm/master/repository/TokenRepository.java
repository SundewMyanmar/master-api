package com.sdm.master.repository;

import com.sdm.core.repository.DefaultRepository;
import com.sdm.master.entity.TokenEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends DefaultRepository<TokenEntity, String> {

    Optional<List<TokenEntity>> findByUserId(long userId);

    Optional<TokenEntity> findByUserIdAndDeviceIdAndDeviceOs(long userId, String deviceId, String deviceOS);

    Optional<TokenEntity> findByDeviceIdAndDeviceOs(String deviceId, String deviceOS);

    Page<TokenEntity> findByLastLoginBetween(Date fromDate, Date toDate, Pageable pageable);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM tbl_user_tokens WHERE user_id = :userId", nativeQuery = true)
    void cleanTokenByUserId(@Param("userId") long userId);

}
