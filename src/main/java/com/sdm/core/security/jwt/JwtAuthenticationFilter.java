package com.sdm.core.security.jwt;

import com.sdm.core.Constants;
import com.sdm.core.exception.InvalidTokenException;
import com.sdm.core.service.ClientService;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.LocaleManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Log4j2
public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    @Autowired
    LocaleManager localeManager;
    @Autowired
    private JwtAuthenticationHandler jwtAuthHandler;
    @Autowired
    private ClientService clientService;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtAuthenticationHandler jwtAuthHandler, ClientService clientService) {
        super(authenticationManager);
        this.jwtAuthHandler = jwtAuthHandler;
        this.clientService = clientService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        if (httpServletRequest.getMethod().equalsIgnoreCase("options")) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        //Check Client Info
        if (clientService.isBlocked(httpServletRequest)) {
            SecurityContextHolder.clearContext();
            httpServletResponse.sendError(HttpStatus.FORBIDDEN.value(), localeManager.getMessage("blocked-auth-user"));
            return;
        }

        String authorization = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);

        if (Globalizer.isNullOrEmpty(authorization)) {
            authorization = httpServletRequest.getParameter(Constants.Auth.PARAM_NAME);
        } else if (authorization.length() > Constants.Auth.TYPE.length()) {
            authorization = authorization.substring(Constants.Auth.TYPE.length()).strip();
        }

        //Check Request with Authorization?
        if (!Globalizer.isNullOrEmpty(authorization)) {
            try {
                UsernamePasswordAuthenticationToken authToken = jwtAuthHandler.authByJwt(authorization, httpServletRequest);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (InvalidTokenException ex) {
                httpServletResponse.sendError(HttpStatus.FORBIDDEN.value(), localeManager.getMessage("auth-token-access-denied"));
                return;
            }
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}