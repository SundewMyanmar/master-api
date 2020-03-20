package com.sdm.auth.service;

import com.sdm.Constants;
import com.sdm.admin.model.User;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.Token;
import com.sdm.auth.repository.TokenRepository;
import com.sdm.core.config.properties.SecurityProperties;
import com.sdm.core.exception.InvalidTokenExcpetion;
import com.sdm.core.model.AuthInfo;
import com.sdm.core.util.jwt.JwtAuthenticationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@Service("jwtAuthHandler")
public class JwtService implements JwtAuthenticationHandler {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    SecurityProperties securityProperties;

    @Override
    @Transactional
    public boolean authByJwt(AuthInfo authInfo, HttpServletRequest request) {
        Token authToken = tokenRepository.findById(authInfo.getToken())
                .orElseThrow(() -> new InvalidTokenExcpetion("There is no token: " + authInfo.getToken()));

        if (!authInfo.isAccountNonExpired()
                || !authInfo.getToken().equalsIgnoreCase(authToken.getId())
                || !authInfo.getDeviceId().equalsIgnoreCase(authToken.getDeviceId())
                || authInfo.getUserId() != authToken.getUser().getId()) {
            throw new InvalidTokenExcpetion("Sorry! requested token is not valid/expired.");
        }

        authInfo.setExpired(authToken.getTokenExpired());

        User userEntity = userRepository.findById(authInfo.getUserId())
                .orElseThrow(() -> new InvalidTokenExcpetion("There is no user: " + authInfo.getToken()));

        if (userEntity.getStatus() != User.Status.ACTIVE) {
            throw new InvalidTokenExcpetion("Sorry! you are not active now. Pls contact to admin.");
        }

        authInfo.addAuthority(Constants.Auth.DEFAULT_USER_ROLE);

        //Is Root?
        if (securityProperties.getOwnerIds().contains(authInfo.getUserId())) {
            authInfo.addAuthority(Constants.Auth.ROOT_ROLE);
        }

        try {
            userEntity.getRoles().forEach(role -> authInfo.addAuthority(Constants.Auth.AUTHORITY_PREFIX + role.getId()));
        } catch (Exception ex) {
            logger.warn(ex.getLocalizedMessage(), ex);
        }

        logger.info(authToken.getUser().getDisplayName() + " login by " + authToken.getId());
        return true;
    }
}
