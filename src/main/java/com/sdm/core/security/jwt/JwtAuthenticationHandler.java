package com.sdm.core.security.jwt;

import com.sdm.core.model.AuthInfo;

import javax.servlet.http.HttpServletRequest;

public interface JwtAuthenticationHandler {
    boolean authByJwt(AuthInfo authInfo, HttpServletRequest request);
}
