package com.sdm.master.service;

import com.sdm.Constants;
import com.sdm.core.exception.InvalidTokenExcpetion;
import com.sdm.core.model.AuthInfo;
import com.sdm.core.security.jwt.JwtAuthenticationHandler;
import com.sdm.master.entity.TokenEntity;
import com.sdm.master.entity.UserEntity;
import com.sdm.master.repository.RoleRepository;
import com.sdm.master.repository.TokenRepository;
import com.sdm.master.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Service("jwtAuthHandler")
public class JwtService implements JwtAuthenticationHandler {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    RoleRepository roleRepository;

    @Override
    @Transactional
    public boolean authByJwt(AuthInfo authInfo, HttpServletRequest request) {
        TokenEntity authToken = tokenRepository.findById(authInfo.getToken())
                .orElseThrow(() -> new InvalidTokenExcpetion("There is no token: " + authInfo.getToken()));

        if (!authInfo.isAccountNonExpired()
                || !authInfo.getToken().equalsIgnoreCase(authToken.getId())
                || !authInfo.getDeviceId().equalsIgnoreCase(authToken.getDeviceId())
                || authInfo.getUserId() != authToken.getUser().getId()) {
            throw new InvalidTokenExcpetion("Sorry! requested token is not valid/expired.");
        }

        authInfo.setExpired(authToken.getTokenExpired());
        authInfo.addAuthority(Constants.Auth.DEFAULT_USER_ROLE);

        UserEntity userEntity = userRepository.findById(authInfo.getUserId())
                .orElseThrow(() -> new InvalidTokenExcpetion("There is no user: " + authInfo.getToken()));

        if (userEntity.getStatus() != UserEntity.Status.ACTIVE) {
            throw new InvalidTokenExcpetion("Sorry! you are not active now. Pls contact to admin.");
        }

        try {
            userEntity.getRoles().forEach(role -> authInfo.addAuthority(role.getName()));
        } catch (Exception ex) {
            logger.warn(ex.getLocalizedMessage(), ex);
        }

        authToken.setLastLogin(new Date());
        logger.info("User " + authToken.getUser().getUserName() + " login by " + authToken.getId());
        tokenRepository.save(authToken);

        return true;
    }
}
