package com.sdm.core.util.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter extends OncePerRequestFilter {

    private String allowedOrigins = "*";

    private String allowedMethods = "GET, PUT, POST, DELETE, OPTIONS, HEAD";

    private String allowedHeaders = "authorization, content-type, xsrf-token,accept";

    private String exposedHeaders = "xsrf-token";

    private long maxAge = 36000;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", this.allowedOrigins);
        response.setHeader("Access-Control-Allow-Methods", this.allowedMethods);
        response.setHeader("Access-Control-Max-Age", String.valueOf(this.maxAge));
        response.setHeader("Access-Control-Allow-Headers", this.allowedHeaders);
        response.addHeader("Access-Control-Expose-Headers", this.exposedHeaders);
        if ("OPTIONS".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
