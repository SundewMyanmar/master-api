package com.sdm.core.security.jwt;

import com.sdm.Constants;
import com.sdm.core.SecurityProperties;
import com.sdm.core.exception.InvalidTokenExcpetion;
import com.sdm.core.security.model.AuthInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.json.JSONArray;
import org.json.JSONObject;
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

    private AuthInfo getAuth(String tokenString, String deviceId) {
        try {
            byte[] jwtKey = Base64.getDecoder().decode(securityProperties.getJwtKey());
            Claims authorizeToken = Jwts.parser().setSigningKey(jwtKey)
                .requireIssuer(deviceId).parseClaimsJws(tokenString).getBody();

            Date expired = authorizeToken.getExpiration();
            if (expired.before(new Date())) {
                throw new InvalidTokenExcpetion("Token has expired.");
            }

            JSONObject subject = new JSONObject(authorizeToken.getSubject());
            long userId = subject.getLong(Constants.Auth.SUBJECT_USER);
            JSONArray roles = subject.getJSONArray(Constants.Auth.SUBJECT_ROLES);

            AuthInfo authInfo = new AuthInfo();
            authInfo.setUserId(userId);
            authInfo.setToken(authorizeToken.getId());
            authInfo.setDeviceId(deviceId);
            authInfo.setExpired(expired);
            for (int i = 0; i < roles.length(); i++) {
                authInfo.addAuthority(roles.getString(i));
            }

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
        final String deviceId = usernamePasswordAuthenticationToken.getCredentials().toString();
        AuthInfo requestAuth = this.getAuth(jwt, deviceId);
        boolean isAllow = jwtAuthHandler.authByJwt(requestAuth);
        if (isAllow) {
            usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                requestAuth, null, requestAuth.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        } else {
            throw new InvalidTokenExcpetion("Sorry! your authorization token hasn't permission to the resource.");
        }
        return requestAuth;
    }
}
