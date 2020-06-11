package com.sdm.core.util.jwt;

import com.sdm.Constants;
import com.sdm.core.config.properties.SecurityProperties;
import com.sdm.core.exception.InvalidTokenExcpetion;
import com.sdm.core.model.AuthInfo;
import com.sdm.core.service.ClientService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Service
@Log4j2
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private JwtAuthenticationHandler jwtAuthHandler;

    @Autowired
    ClientService clientService;

    /**
     * Prevent Bruteforce attack
     *
     * @return
     */
    private int increaseFailedCount(HttpSession session) {
        Integer count = (Integer) session.getAttribute(Constants.SESSION.JWT_FAILED_COUNT);
        if (count == null) {
            count = 0;
        }
        count++;
        session.setAttribute(Constants.SESSION.JWT_FAILED_COUNT, count);
        return count;
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
        String userAgent = httpServletRequest.getHeader(HttpHeaders.USER_AGENT);

        if (StringUtils.isEmpty(authorization)) {
            authorization = httpServletRequest.getParameter(Constants.Auth.PARAM_NAME);
        } else if (authorization.length() > Constants.Auth.TYPE.length()) {
            authorization = authorization.substring(Constants.Auth.TYPE.length()).strip();
        }

        //Check Request with Authorization?
        if (!StringUtils.isEmpty(authorization) && !StringUtils.isEmpty(userAgent)) {
            try {
                AuthInfo authInfo = jwtAuthHandler.authByJwt(authorization, httpServletRequest);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        authInfo, authInfo.getDeviceId(), authInfo.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (InvalidTokenExcpetion ex) {
                increaseFailedCount(httpServletRequest.getSession());
                httpServletResponse.sendError(HttpStatus.UNAUTHORIZED.value(), "Sorry! your authorization token hasn't permission to the resource.");
                return;
            }
        } else {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}