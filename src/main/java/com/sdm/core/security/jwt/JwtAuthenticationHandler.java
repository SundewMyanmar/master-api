package com.sdm.core.security.jwt;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface JwtAuthenticationHandler {
    UsernamePasswordAuthenticationToken authByJwt(String token, HttpServletRequest request);

    void setClientToken(HttpServletRequest request, HttpServletResponse response);
}
