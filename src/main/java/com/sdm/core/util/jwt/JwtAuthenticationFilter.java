package com.sdm.core.util.jwt;

import com.sdm.Constants;
import com.sdm.core.config.properties.SecurityProperties;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.exception.InvalidTokenExcpetion;
import com.sdm.core.model.AuthInfo;
import com.sdm.core.service.ClientService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.tomcat.util.bcel.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private JwtAuthenticationHandler jwtAuthHandler;

    @Autowired
    ClientService clientService;

    /**
     * Set Secrurity Context
     * @param request
     * @param auth
     */
    private void storeAuthInfo (HttpServletRequest request, AuthInfo auth) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                auth, auth.getDeviceId(), auth.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    /**
     * Extract JWT Auth Token by UserAgent
     * @param tokenString
     * @param userAgent
     * @return
     */
    private AuthInfo getAuth(String tokenString, String userAgent) {
        try {
            byte[] jwtKey = Base64.getDecoder().decode(securityProperties.getJwtKey());
            Claims authorizeToken = Jwts.parser().setSigningKey(jwtKey)
                    .requireIssuer(userAgent).parseClaimsJws(tokenString).getBody();

            Date expired = authorizeToken.getExpiration();
            if (expired.before(new Date())) {
                throw new InvalidTokenExcpetion("Token has expired.");
            }

            int userId = Integer.parseInt(authorizeToken.getSubject());
            String deviceId = authorizeToken.get("deviceId").toString();
            String deviceOs = authorizeToken.get("deviceOs").toString();

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

    /**
     * Check Requested Authorization Header or AccessToken
     * @param request
     */
    private void checkAuthToken(HttpServletRequest request){
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);

        if (StringUtils.isEmpty(authorization)) {
            authorization = request.getParameter(Constants.Auth.PARAM_NAME);
        }else if(authorization.length() > Constants.Auth.TYPE.length()){
            authorization = authorization.substring(Constants.Auth.TYPE.length()).strip();
        }

        if (!StringUtils.isEmpty(authorization) && !StringUtils.isEmpty(userAgent)) {
            AuthInfo requestAuth = this.getAuth(authorization, userAgent);
            boolean isAllow = jwtAuthHandler.authByJwt(requestAuth, null);
            if (isAllow) {
                storeAuthInfo(request, requestAuth);
            } else {
                SecurityContextHolder.clearContext();
                throw new InvalidTokenExcpetion("Sorry! your authorization token hasn't permission to the resource.");
            }
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        if(httpServletRequest.getMethod().equalsIgnoreCase("options")){
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        //Check Client Info
        if(clientService.isBlocked(httpServletRequest)){
            SecurityContextHolder.clearContext();
            httpServletResponse.sendError(HttpStatus.FORBIDDEN.value(), "Blocked! Please contact to authority person.");
            return;
        }

        this.checkAuthToken(httpServletRequest);
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }


}