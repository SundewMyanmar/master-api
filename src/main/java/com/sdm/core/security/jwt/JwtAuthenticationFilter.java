package com.sdm.core.security.jwt;

import com.sdm.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        Optional<String> authorization = Optional.ofNullable(httpServletRequest.getHeader(Constants.Auth.HEADER_TOKEN_KEY));
        Optional<String> deviceId = Optional.ofNullable(httpServletRequest.getHeader(Constants.Auth.HEADER_CREDENTIAL_KEY));

        if (authorization.isPresent() && deviceId.isPresent()) {
            String tokenString = authorization.get().substring(Constants.Auth.TYPE.length()).trim();
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                tokenString, deviceId.get());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }


}
