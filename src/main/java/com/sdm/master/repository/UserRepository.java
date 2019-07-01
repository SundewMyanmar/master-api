package com.sdm.master.repository;

import com.sdm.core.repository.DefaultRepository;
import com.sdm.master.entity.UserEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends DefaultRepository<UserEntity, Long> {
    Optional<UserEntity> findByUserNameOrEmail(String userName, String email);

    Optional<UserEntity> findByEmailAndOtpToken(String email, String token);

    Optional<UserEntity> findByFacebookId(String facebookId);

    @Query("SELECT u FROM UserEntity u WHERE u.status = 'ACTIVE' AND (u.email = :user OR u.userName = :user) AND u.password = :password")
    Optional<UserEntity> authByPassword(@Param("user") String user, @Param("password") String password);
}
