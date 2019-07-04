package com.sdm.core.security.jwt;

import com.sdm.Constants;
import com.sdm.core.config.properties.SecurityProperties;
import com.sdm.core.exception.InvalidTokenExcpetion;
import com.sdm.core.model.AuthInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
public class JwtAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationProvider.class);

    @Autowired
    private JwtAuthenticationHandler jwtAuthHandler;

    @Autowired
    SecurityProperties securityProperties;

    private AuthInfo getAuth(String tokenString, String userAgent) {
        try {
            byte[] jwtKey = Base64.getDecoder().decode(securityProperties.getJwtKey());
            Claims authorizeToken = Jwts.parser().setSigningKey(jwtKey)
                    .requireIssuer(userAgent).parseClaimsJws(tokenString).getBody();

            Date expired = authorizeToken.getExpiration();
            if (expired.before(new Date())) {
                throw new InvalidTokenExcpetion("Token has expired.");
            }

            long userId = Long.parseLong(authorizeToken.getSubject());
            String deviceId = authorizeToken.get("device_id").toString();
            String deviceOs = authorizeToken.get("device_os").toString();

            AuthInfo authInfo = new AuthInfo();
            authInfo.setUserId(userId);
            authInfo.setToken(authorizeToken.getId());
            authInfo.setDeviceId(deviceId);
            authInfo.setDeviceOs(deviceOs);
            authInfo.setExpired(expired);

            return authInfo;
        } catch (Exception ex) {
            throw new InvalidTokenExcpetion(ex.getLocalizedMessage());
        }
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {

    }

    @Override
    protected UserDetails retrieveUser(String s, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
        final String jwt = usernamePasswordAuthenticationToken.getPrincipal().toString();
        final String userAgent = usernamePasswordAuthenticationToken.getCredentials().toString();
        AuthInfo requestAuth = this.getAuth(jwt, userAgent);
        boolean isAllow = jwtAuthHandler.authByJwt(requestAuth, null);
        if (isAllow) {
            if (securityProperties.getOwnerIds().contains(requestAuth.getUserId())) {
                requestAuth.addAuthority(Constants.Auth.ROOT_ROLE);
            }

            usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                    requestAuth, requestAuth.getDeviceId(), requestAuth.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        } else {
            SecurityContextHolder.clearContext();
            throw new InvalidTokenExcpetion("Sorry! your authorization token hasn't permission to the resource.");
        }
        return requestAuth;
    }
}
