package com.sdm.core.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CorsFilter extends OncePerRequestFilter {

    @Value("${com.sdm.cors.allow-origins}")
    private String allowedOrigins = "*";

    @Value("${com.sdm.cors.allow-methods}")
    private String allowedMethods = "GET, PUT, POST, DELETE, OPTIONS, HEAD";

    @Value("${com.sdm.cors.allow-headers}")
    private String allowedHeaders = "authorization, content-type, xsrf-token,accept";

    @Value("${com.sdm.cors.exposed-headers}")
    private String exposedHeaders = "xsrf-token";

    @Value("${com.sdm.cors.allow-credentials}")
    private boolean allowCredentials = true;

    @Value("${com.sdm.cors.max-age}")
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

    public String getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public String getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(String allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public String getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(String allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public String getExposedHeaders() {
        return exposedHeaders;
    }

    public void setExposedHeaders(String exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }
}
