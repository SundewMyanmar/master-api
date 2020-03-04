package com.sdm.admin.repository;

import com.sdm.admin.model.User;
import com.sdm.core.db.DefaultRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends DefaultRepository<User, Integer> {
    Optional<User> findOneByPhoneNumberOrEmail(String phoneNumber, String email);

    Optional<User> findOneByPhoneNumberAndEmail(String phoneNumber, String email);

    @Query("SELECT u FROM admin.UserEntity u WHERE u.status = 'ACTIVE' AND (u.email = :user OR u.phoneNumber = :user) AND u.otpToken = :token")
    Optional<User> checkOTP(@Param("user") String user, @Param("token") String token);

    Optional<User> findOneByFacebookId(String facebookId);

    @Query("SELECT u FROM admin.UserEntity u WHERE u.status = 'ACTIVE' AND (u.email = :user OR u.phoneNumber = :user) AND u.password = :password")
    Optional<User> authByPassword(@Param("user") String user, @Param("password") String password);
}
