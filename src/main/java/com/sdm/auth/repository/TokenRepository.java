package com.sdm.auth.repository;

import com.sdm.auth.model.Token;
import com.sdm.core.db.repository.DefaultRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends DefaultRepository<Token, String> {

    Optional<List<Token>> findByUserId(int userId);

    Optional<Token> findFirstByDeviceId(String deviceId);

    Optional<Token> findFirstByFirebaseMessagingToken(String firebaseMessagingToken);

    boolean existsByIdAndUserIdAndDeviceIdAndDeviceOs(String id, int userId, String deviceId, String deviceOs);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM tbl_auth_tokens WHERE user_id = :userId", nativeQuery = true)
    void cleanTokenByUserId(@Param("userId") int userId);
}
