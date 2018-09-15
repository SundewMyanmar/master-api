package com.sdm.core.config;

import com.sdm.core.security.PermissionHandler;
import com.sdm.core.security.jwt.JwtAuthenticationFilter;
import com.sdm.core.security.jwt.JwtAuthenticationProvider;
import com.sdm.core.security.jwt.JwtUnauthorizeHandler;
import com.sdm.core.security.model.PermissionMatcher;
import com.sdm.master.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
    prePostEnabled = true
)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUnauthorizeHandler jwtUnauthorizeHandler;

    @Autowired
    private JwtAuthenticationProvider jwtAuthenticationProvider;

    @Autowired
    private PermissionHandler permissionHandler;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(jwtAuthenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and().exceptionHandling()
            .authenticationEntryPoint(jwtUnauthorizeHandler)
            .and().authenticationProvider(jwtAuthenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter(), AnonymousAuthenticationFilter.class)
            .csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .logout().disable();

        List<PermissionMatcher> matchers = permissionHandler.getAllMatchers();
        for (PermissionMatcher matcher : matchers) {
            if (matcher.getPattern() == null || matcher.getPattern().isEmpty()) {
                continue;
            }

            if (matcher.isEveryone()) {
                http.authorizeRequests()
                    .antMatchers(matcher.getMethod(), matcher.getPattern())
                    .permitAll();
            } else if (matcher.isUser()) {
                http.authorizeRequests()
                    .antMatchers(matcher.getMethod(), matcher.getPattern())
                    .authenticated();
            } else if (matcher.getRole() != null) {
                http.authorizeRequests()
                    .antMatchers(matcher.getMethod(), matcher.getPattern())
                    .hasRole(matcher.getRole());
            }
        }
    }
}
