package com.sdm.core.jwt;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import javax.servlet.http.HttpServletRequest;

public interface JwtAuthenticationHandler {
    UsernamePasswordAuthenticationToken authByJwt(String token, HttpServletRequest request);
}
