package com.sdm.master.repository;

import com.sdm.master.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsernameOrEmail(String username, String email);

    Optional<UserEntity> findByEmailAndOtpToken(String email, String token);

    Optional<UserEntity> findByFacebookId(String facebookId);

    @Query("SELECT u FROM UserEntity u WHERE (u.email = :user OR u.username = :user) AND u.password = :password")
    Optional<UserEntity> authByPassword(@Param("user") String user, @Param("password") String password);
}
