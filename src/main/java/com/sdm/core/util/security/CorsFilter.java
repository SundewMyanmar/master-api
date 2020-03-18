package com.sdm.core.util.security;

import com.sdm.core.config.properties.CORSProperties;
import org.springframework.context.annotation.Configuration;
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

    private CORSProperties properties;

    public CorsFilter(CORSProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", properties.getAllowedOrigins());
        response.setHeader("Access-Control-Allow-Methods", properties.getAllowedMethods());
        response.setHeader("Access-Control-Max-Age", String.valueOf(properties.getMaxAge()));
        response.setHeader("Access-Control-Allow-Headers", properties.getAllowedHeaders());
        response.addHeader("Access-Control-Expose-Headers", properties.getExposedHeaders());
        response.addHeader("Access-Control-Allow-Credentials", String.valueOf(properties.getAllowedCredentials()));
        if ("OPTIONS".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
