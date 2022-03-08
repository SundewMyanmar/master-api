package com.sdm.admin.config;

import com.sdm.core.Constants;
import com.sdm.core.security.PermissionHandler;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.security.jwt.JwtAuthenticationFilter;
import com.sdm.core.security.jwt.JwtAuthenticationHandler;
import com.sdm.core.security.jwt.JwtUnauthorizeHandler;
import com.sdm.core.service.ClientService;
import com.sdm.core.util.Globalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String[] SYSTEM_WHITE_LIST = {
            "/",
            "/error",
            "/public/**",
            "/auth/**",
            "/mfa/resend",

            //Don't forget to remove in Production Mode
            "/setup",
            "/util/**",
            "/webjars/**",
            "/v2/api-docs",
            "/swagger-ui/**",
            "/swagger-resources/**",
    };
    public static final String[] USER_PERMISSION_LIST = {
            "/me/**",
            "/admin/menus/me",
            "/notifications/me/**",
            "/files/**",
            "/mfa/**"
    };
    public static final String[] ROOT_PERMISSION_LIST = {
            "/system/config",
    };

    @Autowired
    private JwtUnauthorizeHandler jwtUnauthorizeHandler;

    @Autowired
    private JwtAuthenticationHandler jwtAuthenticationHandler;

    @Autowired
    private ClientService clientService;

    @Autowired
    private PermissionHandler permissionHandler;

    @Autowired
    private SecurityManager securityManager;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setSameSite("strict");
        if (!Globalizer.isNullOrEmpty(securityManager.getProperties().getCookieDomain())) {
            serializer.setDomainName(securityManager.getProperties().getCookieDomain());
        }

        if (!Globalizer.isNullOrEmpty(securityManager.getProperties().getCookiePath())) {
            serializer.setCookiePath(securityManager.getProperties().getCookiePath());
        }
        return serializer;
    }

    @Bean
    JwtAuthenticationFilter authenticationFilter() throws Exception {
        return new JwtAuthenticationFilter(authenticationManager(), jwtAuthenticationHandler, clientService);
    }

    /**
     * To fix XSRF cookie error if admin panel and API domain is not much.
     *
     * @return
     */
    private CsrfTokenRepository getCsrfTokenRepository() {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        if (!Globalizer.isNullOrEmpty(securityManager.getProperties().getCookieDomain())) {
            csrfTokenRepository.setCookieDomain(securityManager.getProperties().getCookieDomain());
        }

        if (!Globalizer.isNullOrEmpty(securityManager.getProperties().getCookiePath())) {
            csrfTokenRepository.setCookiePath(securityManager.getProperties().getCookiePath());
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
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).and()
                .exceptionHandling().authenticationEntryPoint(jwtUnauthorizeHandler).and()
                .addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests(authorize -> authorize
                        .antMatchers(SYSTEM_WHITE_LIST).permitAll()
                        .antMatchers(USER_PERMISSION_LIST).hasAuthority(Constants.Auth.DEFAULT_USER_ROLE)
                        .antMatchers(ROOT_PERMISSION_LIST).hasAuthority(Constants.Auth.ROOT_ROLE)
                        .anyRequest().access("@permissionHandler.check(authentication, request)")
                );

        if (securityManager.getProperties().isCsrfEnable()) {
            http.csrf().csrfTokenRepository(this.getCsrfTokenRepository());
        } else {
            http.csrf().disable();
        }
    }


}