package com.sdm.core.security.jwt;

import com.sdm.Constants;
import com.sdm.core.exception.InvalidTokenExcpetion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        try {
            String authorization = httpServletRequest.getHeader(Constants.Auth.HEADER_TOKEN_KEY);
            if (authorization == null || authorization.isEmpty()) {
                throw new InvalidTokenExcpetion("Can't find authorization token.");
            }

            String deviceId = httpServletRequest.getHeader(Constants.Auth.HEADER_CREDENTIAL_KEY);
            if (deviceId == null || deviceId.isEmpty()) {
                throw new InvalidTokenExcpetion("Can't find device unique id.");
            }

            String tokenString = authorization.substring(Constants.Auth.TYPE.length()).trim();
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                tokenString, deviceId);
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (AuthenticationException ex) {
            logger.error(ex.getLocalizedMessage());
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }


}
