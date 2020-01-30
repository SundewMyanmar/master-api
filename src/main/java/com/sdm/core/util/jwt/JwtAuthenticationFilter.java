package com.sdm.core.util.jwt;

import com.sdm.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
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
        boolean isHeader = true;
        Optional<String> authorization = Optional.ofNullable(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION));
        Optional<String> userAgent = Optional.ofNullable(httpServletRequest.getHeader(HttpHeaders.USER_AGENT));

        if (!authorization.isPresent()) {
            authorization = Optional.ofNullable(httpServletRequest.getParameter(Constants.Auth.PARAM_NAME));
            isHeader = false;
        }

        if (authorization.isPresent() && userAgent.isPresent()) {
            String tokenString = authorization.get();
            if (isHeader) {
                tokenString = tokenString.substring(Constants.Auth.TYPE.length()).strip();
            }
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    tokenString, userAgent.get());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }


}