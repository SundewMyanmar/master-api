package com.sdm.core.util.jwt;

import com.sdm.core.model.AuthInfo;

import javax.servlet.http.HttpServletRequest;

public interface JwtAuthenticationHandler {
    AuthInfo authByJwt(String token, HttpServletRequest request);
}
