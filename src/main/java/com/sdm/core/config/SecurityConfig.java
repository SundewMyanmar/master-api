package com.sdm.core.config;

import com.sdm.Constants;
import com.sdm.core.util.jwt.JwtAuthenticationFilter;
import com.sdm.core.util.jwt.JwtAuthenticationProvider;
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

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtUnauthorizeHandler jwtUnauthorizeHandler;

    @Autowired
    private JwtAuthenticationProvider jwtAuthenticationProvider;

    @Autowired
    private PermissionHandler permissionHandler;

    @Bean
    public JwtAuthenticationFilter authenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    private static final String[] SYSTEM_WHITE_LIST = {
            "/",
            "/error",
            "/facebook/messenger",
            "/util/**",
            "/public/**",
            "/auth/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/v2/api-docs",
    };

    private static final String[] USER_PERMISSION_LIST = {
            "/me/**",
            "/admin/menus/me",
            "/files/**"
    };

    private static final String[] ROOT_PERMISSION_LIST = {
            "/actuator/**",
    };

    /**
     * Warning! HttpSecurity authorize validation process run step by step.
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().exceptionHandling().authenticationEntryPoint(jwtUnauthorizeHandler)
                .and().formLogin().disable()
                .httpBasic().disable()
                .logout().disable()
                .csrf().disable()
                .httpBasic()
                .and().addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(jwtAuthenticationProvider)
                .authorizeRequests(authorize -> authorize
                        .antMatchers(SYSTEM_WHITE_LIST).permitAll()
                        .antMatchers(USER_PERMISSION_LIST).hasAuthority(Constants.Auth.DEFAULT_USER_ROLE)
                        .antMatchers(ROOT_PERMISSION_LIST).hasAuthority(Constants.Auth.ROOT_ROLE)
                        .anyRequest().access("@permissionHandler.check(authentication, request)")
                );
    }
}