package com.sdm.core.config;

import com.sdm.Constants;
import com.sdm.core.util.jwt.JwtAuthenticationFilter;
import com.sdm.core.util.jwt.JwtAuthenticationProvider;
import com.sdm.core.util.jwt.JwtUnauthorizeHandler;
import com.sdm.core.util.security.PermissionHandler;
import com.sdm.core.util.security.PermissionMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        prePostEnabled = true
)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtUnauthorizeHandler jwtUnauthorizeHandler;

    @Autowired
    private PermissionHandler permissionHandler;

    @Autowired
    private JwtAuthenticationProvider jwtAuthenticationProvider;

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

        //1. Load default system configure
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().exceptionHandling().authenticationEntryPoint(jwtUnauthorizeHandler)
                .and().csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .logout().disable()
                .httpBasic().and()
                .addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(jwtAuthenticationProvider)
                .authorizeRequests()
                .antMatchers(SYSTEM_WHITE_LIST).permitAll()
                .antMatchers(ROOT_PERMISSION_LIST).hasAuthority(Constants.Auth.ROOT_ROLE);

        //2. Load Database Permissions
        List<PermissionMatcher> permissions = permissionHandler.loadPermissions();

        if (permissions != null) {
            for (PermissionMatcher permission : permissions) {
                Optional<String> pattern = Optional.ofNullable(permission.getPattern());
                HttpMethod method = permission.getMethod();
                Set<String> roles = permission.getRoles();
                roles.add(Constants.Auth.ROOT_ROLE);

                if (pattern.isPresent() && roles.size() > 0) {
                    String[] allowedRoles = new String[roles.size()];
                    roles.toArray(allowedRoles);
                    String cleanPattern = pattern.get().strip();

                    http.authorizeRequests()
                            .antMatchers(method, cleanPattern.split(","))
                            .hasAnyAuthority(allowedRoles);
                }
            }
        }

        //3. Prevent
        http.authorizeRequests().anyRequest().authenticated();
    }
}