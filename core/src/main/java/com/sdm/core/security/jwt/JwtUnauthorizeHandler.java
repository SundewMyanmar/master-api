package com.sdm.core.security.jwt;

import com.sdm.core.util.LocaleManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtUnauthorizeHandler implements AuthenticationEntryPoint {
    @Autowired
    LocaleManager localeManager;

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, localeManager.getMessage("resource-access-denied"));
    }
}