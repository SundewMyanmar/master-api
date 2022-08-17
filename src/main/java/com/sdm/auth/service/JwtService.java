package com.sdm.auth.service;

import com.sdm.admin.model.User;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.Token;
import com.sdm.auth.model.request.TokenInfo;
import com.sdm.auth.repository.TokenRepository;
import com.sdm.core.Constants;
import com.sdm.core.exception.InvalidTokenException;
import com.sdm.core.model.AuthInfo;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.security.jwt.JwtAuthenticationHandler;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.LocaleManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service("jwtAuthHandler")
@Log4j2
public class JwtService implements JwtAuthenticationHandler {
    @Value("${com.sdm.security.jwt-key:v5OtLen4XOumAFTmp4vPsgtXoHzidfwoJEIITGDrfX2BUUd09ToF4kFMbRsX0cV7/7rRQzU1/BhsJ/OGPJ61tA==}")
    private String jwtKey;

    private static final String CLIENT_TOKEN = "C-TOKEN";
    private static final String CLAIM_DEVICE_ID = "deviceId";
    private static final String CLAIM_DEVICE_OS = "deviceOs";
    private static final String CLAIM_ROLES = "roles";

    @Autowired
    private HttpServletResponse servletResponse;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private LocaleManager localeManager;

    private Date getTokenExpired() {
        return Globalizer.addDate(new Date(), Duration.ofDays(securityManager.getProperties().getAuthTokenDayOfLife()));
    }

    private String getAudience(HttpServletRequest request) {
        Cookie clientToken = WebUtils.getCookie(request, CLIENT_TOKEN);
        if(Globalizer.isNullOrEmpty(clientToken)){
            return "";
        }

        return clientToken.getValue();
    }

    private String getIssuer(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

    private SecretKey getKey() {
        byte[] decodeKey = Decoders.BASE64.decode(jwtKey);
        return Keys.hmacShaKeyFor(decodeKey);
    }

    private String buildRoles(User user) {
        Set<String> roles = new HashSet<>();
        //Set Default User Role
        roles.add(Constants.Auth.DEFAULT_USER_ROLE);

        //Is Root?
        if (securityManager.getProperties().getOwnerIds().contains(user.getId())) {
            roles.add(Constants.Auth.ROOT_ROLE);
        }

        //Set DB Roles
        try {
            user.getRoles().forEach(role -> roles.add(Constants.Auth.AUTHORITY_PREFIX + role.getId()));
        } catch (Exception ex) {
            log.warn(ex.getLocalizedMessage(), ex);
        }
        return String.join(",", roles);
    }

    @Transactional
    private String generateJWT(Token token, HttpServletRequest request) {
        token.setTokenExpired(getTokenExpired());
        token.setLastLogin(new Date());
        tokenRepository.save(token);

        String userId = token.getUser().getId().toString();
        String roles = buildRoles(token.getUser());
        String aud = getAudience(request);
        String iss = getIssuer(request);

        return Jwts.builder().setId(token.getId())
                .setSubject(userId)
                .claim(CLAIM_DEVICE_ID, token.getDeviceId())
                .claim(CLAIM_DEVICE_OS, token.getDeviceOs())
                .claim(CLAIM_ROLES, roles)
                .setAudience(aud)
                .setIssuer(iss)
                .setIssuedAt(new Date())
                .setExpiration(token.getTokenExpired())
                .compressWith(CompressionCodecs.DEFLATE)
                .signWith(getKey()).compact();
    }

    public String createToken(User user, TokenInfo tokenInfo, HttpServletRequest request) {
        Token token = tokenRepository.findFirstByDeviceId(tokenInfo.getDeviceId())
                .orElseGet(() -> {
                    Token newToken = new Token();
                    newToken.setId(UUID.randomUUID().toString());
                    return newToken;
                });

        token.setUser(user);
        token.setDeviceId(tokenInfo.getDeviceId());
        token.setDeviceOs(tokenInfo.getDeviceOS());
        if (!Globalizer.isNullOrEmpty(tokenInfo.getFirebaseMessagingToken())) {
            token.setFirebaseMessagingToken(tokenInfo.getFirebaseMessagingToken());
        }

        // Generate and store JWT
        String tokenString = this.generateJWT(token, request);

        user.setCurrentToken(tokenString);
        return token.getId();
    }

    @Override
    public void setClientToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie tokenCookie = WebUtils.getCookie(request, CLIENT_TOKEN);
        if(Globalizer.isNullOrEmpty(tokenCookie)){
            tokenCookie = new Cookie(CLIENT_TOKEN, UUID.randomUUID().toString());
            if (!Globalizer.isNullOrEmpty(securityManager.getProperties().getCookieDomain())) {
                tokenCookie.setDomain(securityManager.getProperties().getCookieDomain());
            }
            if (!Globalizer.isNullOrEmpty(securityManager.getProperties().getCookiePath())) {
                tokenCookie.setPath(securityManager.getProperties().getCookiePath());
            }
        }
        response.addCookie(tokenCookie);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional
    public UsernamePasswordAuthenticationToken authByJwt(String jwtString, HttpServletRequest request) throws InvalidTokenException {
        String aud = getAudience(request);
        String iss = getIssuer(request);
        try {
            Claims authorizeToken = Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .requireAudience(aud)
                    .requireIssuer(iss)
                    .build().parseClaimsJws(jwtString).getBody();

            Date expired = authorizeToken.getExpiration();
            if (expired.before(new Date())) {
                throw new InvalidTokenException(localeManager.getMessage("auth-token-expired"));
            }

            int userId = Integer.parseInt(authorizeToken.getSubject());
            String tokenId = authorizeToken.getId();
            String deviceId = authorizeToken.get(CLAIM_DEVICE_ID).toString();
            String deviceOs = authorizeToken.get(CLAIM_DEVICE_OS).toString();
            String roles = authorizeToken.get(CLAIM_ROLES).toString();

            boolean allowed = tokenRepository.existsByIdAndUserIdAndDeviceIdAndDeviceOs(
                    tokenId, userId, deviceId, deviceOs);
            if (!allowed) {
                throw new InvalidTokenException(localeManager.getMessage("resource-access-denied"));
            }

            log.info(String.format("User Id [%d] login by => %s", userId, tokenId));

            AuthInfo authInfo = new AuthInfo();
            authInfo.setUserId(userId);
            authInfo.setToken(authorizeToken.getId());
            authInfo.setDeviceId(deviceId);
            authInfo.setDeviceOs(deviceOs);
            authInfo.setExpired(expired);
            for (String role : roles.split(",")) {
                authInfo.addAuthority(role);
            }
            return new UsernamePasswordAuthenticationToken(authInfo, aud, authInfo.getAuthorities());
        } catch (Exception ex) {
            throw new InvalidTokenException(ex.getLocalizedMessage());
        }
    }
}
