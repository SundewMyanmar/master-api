package com.sdm.core.config;

import com.sdm.Constants;
import com.sdm.core.config.properties.CorsProperties;
import com.sdm.core.config.properties.SecurityProperties;
import com.sdm.core.util.jwt.JwtAuthenticationFilter;
import com.sdm.core.util.jwt.JwtUnauthorizeHandler;
import com.sdm.core.util.security.PermissionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtUnauthorizeHandler jwtUnauthorizeHandler;

    @Autowired
    private PermissionHandler permissionHandler;

    @Autowired
    private CorsProperties corsProperties;

    @Autowired
    private SecurityProperties securityProperties;

    @Bean
    JwtAuthenticationFilter authenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(corsProperties.getAllowedOrigins()));
        configuration.setAllowedMethods(Arrays.asList(corsProperties.getAllowedMethods()));
        configuration.setAllowedHeaders(Arrays.asList(corsProperties.getAllowedHeaders()));
        configuration.setExposedHeaders(Arrays.asList(corsProperties.getExposedHeaders()));
        configuration.setMaxAge(corsProperties.getMaxAge());
        configuration.setAllowCredentials(corsProperties.getAllowedCredential());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    public static final String[] SYSTEM_WHITE_LIST = {
            "/",
            "/error",
            "/facebook/messenger",
            "/util/**",
            "/public/**",
            "/auth/**",
            "/webjars/**",

            //Don't forget to remove in Production Mode
            "/v2/api-docs",
            "/swagger-ui.html",
            "/swagger-resources/**",
    };

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        if (!StringUtils.isEmpty(securityProperties.getCookieDomain())) {
            serializer.setDomainName(securityProperties.getCookieDomain());
        }

        if (!StringUtils.isEmpty(securityProperties.getCookiePath())) {
            serializer.setCookiePath(securityProperties.getCookiePath());
        }
        return serializer;
    }

    public static final String[] USER_PERMISSION_LIST = {
            "/me/**",
            "/admin/menus/me",
            "/files/**"
    };

    public static final String[] ROOT_PERMISSION_LIST = {
            "/actuator/**",
    };

    /**
     * To fix XSRF cookie error if admin panel and API domain is not much.
     *
     * @return
     */
    private CsrfTokenRepository getCsrfTokenRepository() {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        if (!StringUtils.isEmpty(securityProperties.getCookieDomain())) {
            csrfTokenRepository.setCookieDomain(securityProperties.getCookieDomain());
        }

        if (!StringUtils.isEmpty(securityProperties.getCookiePath())) {
            csrfTokenRepository.setCookiePath(securityProperties.getCookiePath());
        }
        return csrfTokenRepository;
    }

    /**
     * Warning! HttpSecurity authorize validation process run step by step.
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin().disable().httpBasic().disable().logout().disable()
                .cors().and()
                .csrf().csrfTokenRepository(this.getCsrfTokenRepository()).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).and()
                .exceptionHandling().authenticationEntryPoint(jwtUnauthorizeHandler).and()
                .addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests(authorize -> authorize
                        .antMatchers(SYSTEM_WHITE_LIST).permitAll()
                        .antMatchers(USER_PERMISSION_LIST).hasAuthority(Constants.Auth.DEFAULT_USER_ROLE)
                        .antMatchers(ROOT_PERMISSION_LIST).hasAuthority(Constants.Auth.ROOT_ROLE)
                        .anyRequest().access("@permissionHandler.check(authentication, request)")
                );
    }


}