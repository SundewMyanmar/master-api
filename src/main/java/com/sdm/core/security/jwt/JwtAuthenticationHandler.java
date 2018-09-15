package com.sdm.core.security.jwt;

import com.sdm.core.security.model.AuthInfo;

public interface JwtAuthenticationHandler {
    boolean authByJwt(AuthInfo authInfo);
}
