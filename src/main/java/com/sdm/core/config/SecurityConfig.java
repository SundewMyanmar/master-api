package com.sdm.core.config;

import com.sdm.Constants;
import com.sdm.core.SecurityProperties;
import com.sdm.core.security.PermissionHandler;
import com.sdm.core.security.PermissionMatcher;
import com.sdm.core.security.jwt.JwtAuthenticationFilter;
import com.sdm.core.security.jwt.JwtAuthenticationProvider;
import com.sdm.core.security.jwt.JwtUnauthorizeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;
import java.util.Optional;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
    prePostEnabled = true
)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private JwtUnauthorizeHandler unauthorizeHandler;

    @Autowired
    private JwtAuthenticationProvider authenticationProvider;

    @Autowired
    private PermissionHandler permissionHandler;

    @Bean
    public JwtAuthenticationFilter authenticationFilter() {
        return new JwtAuthenticationFilter();
    }

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
            .and().exceptionHandling().authenticationEntryPoint(unauthorizeHandler)
            .and().csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .logout().disable()
            .addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .authenticationProvider(authenticationProvider)
            .authorizeRequests().antMatchers(securityProperties.getPublicUrls()).permitAll();

        //2. Load Database Permissions
        List<PermissionMatcher> permissions = permissionHandler.loadPermissions();

        if (permissions != null) {
            for (PermissionMatcher permission : permissions) {
                Optional<String> pattern = Optional.ofNullable(permission.getPattern());
                Optional<String> role = Optional.ofNullable(permission.getRole());
                if (pattern.isPresent() && role.isPresent()) {
                    String cleanPattern = pattern.get().trim();
                    http.authorizeRequests()
                        .antMatchers(permission.getMethod(), cleanPattern.split(","))
                        .hasAnyRole(role.get(), Constants.Auth.ROOT_ROLE);
                }
            }
        }

        //3. Prevent
        http.authorizeRequests().anyRequest().authenticated();

    }
}
