package com.sdm.admin.repository;

import com.sdm.admin.model.User;
import com.sdm.core.db.repository.DefaultRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends DefaultRepository<User, Integer> {
    Optional<User> findFirstByPhoneNumberOrEmail(String phoneNumber, String email);

    Optional<User> findFirstByPhoneNumberAndEmail(String phoneNumber, String email);

    @Query("SELECT u FROM #{#entityName} u WHERE (u.email = :user OR u.phoneNumber = :user) AND u.otpToken = :token")
    Optional<User> checkOTP(@Param("user") String user, @Param("token") String token);

    Optional<User> findFirstByFacebookId(String facebookId);

    Optional<User> findFirstByGoogleId(String googleId);

    @Query("SELECT u FROM #{#entityName} u WHERE u.status = 'ACTIVE' AND (u.email = :user OR u.phoneNumber = :user) AND u.password = :password")
    Optional<User> authByPassword(@Param("user") String user, @Param("password") String password);

    @Query("SELECT u FROM #{#entityName} u JOIN u.roles r WHERE u.status='ACTIVE' AND r.id=:role")
    List<User> findUsersByRoles(@Param("role") Integer role);
}
