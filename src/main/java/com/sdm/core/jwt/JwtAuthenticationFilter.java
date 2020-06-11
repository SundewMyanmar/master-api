package com.sdm.core.jwt;

import com.sdm.Constants;
import com.sdm.core.exception.InvalidTokenExcpetion;
import com.sdm.core.service.ClientService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Log4j2
public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    private JwtAuthenticationHandler jwtAuthHandler;

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
            httpServletResponse.sendError(HttpStatus.FORBIDDEN.value(), "Blocked! Please contact to authority person.");
            return;
        }

        String authorization = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.isEmpty(authorization)) {
            authorization = httpServletRequest.getParameter(Constants.Auth.PARAM_NAME);
        } else if (authorization.length() > Constants.Auth.TYPE.length()) {
            authorization = authorization.substring(Constants.Auth.TYPE.length()).strip();
        }

        //Check Request with Authorization?
        if (!StringUtils.isEmpty(authorization)) {
            try {
                UsernamePasswordAuthenticationToken authToken = jwtAuthHandler.authByJwt(authorization, httpServletRequest);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (InvalidTokenExcpetion ex) {
                httpServletResponse.sendError(HttpStatus.UNAUTHORIZED.value(), "Sorry! your authorization token hasn't permission to the resource.");
                return;
            }
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}