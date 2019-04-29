package com.sdm.core.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ObjectArrays;
import com.sdm.Constants;
import com.sdm.core.security.CorsFilter;
import com.sdm.core.security.PermissionHandler;
import com.sdm.core.security.PermissionMatcher;
import com.sdm.core.security.jwt.JwtAuthenticationFilter;
import com.sdm.core.security.jwt.JwtAuthenticationProvider;
import com.sdm.core.security.jwt.JwtUnauthorizeHandler;
import com.sdm.master.entity.RoleEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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

    private static final String[] SWAGGER_WHITE_LIST = {
        "/swagger-ui.html",
        "/swagger-resources/**",
        "/v2/api-docs",
        "/webjars/**",
    };

    private static final String[] ROOT_PERMISSION_LIST = {
        "/users/**"
    };

    /**
     * Warning! HttpSecurity authorize validation process run step by step.
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        String[] publicUrls = ObjectArrays.concat(SWAGGER_WHITE_LIST, 
            securityProperties.getPublicUrls(), String.class);

        //1. Load default system configure
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and().exceptionHandling().authenticationEntryPoint(unauthorizeHandler)
            .and().csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .logout().disable()
            .httpBasic().and()
            //Cors Added
            .addFilterBefore(new CorsFilter(), ChannelProcessingFilter.class)
            .addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .authenticationProvider(authenticationProvider)
            .authorizeRequests()
            .antMatchers(publicUrls).permitAll()
            .antMatchers(ROOT_PERMISSION_LIST).hasAuthority(Constants.Auth.ROOT_ROLE);

        //2. Load Database Permissions
        List<PermissionMatcher> permissions = permissionHandler.loadPermissions();

        if (permissions != null) {
            for (PermissionMatcher permission : permissions) {
                Optional<String> pattern = Optional.ofNullable(permission.getPattern());
                HttpMethod method = permission.getMethod();
                List<RoleEntity> roles = new ArrayList<>(permission.getRoles());

                if (pattern.isPresent() && roles.size() > 0) {
                    String[] _roles = new String[roles.size() + 1];
                    _roles[roles.size()] = Constants.Auth.ROOT_ROLE;
                    for (int i = 0; i < roles.size(); i++) {
                        _roles[i] = roles.get(i).getName();
                    }

                    String cleanPattern = pattern.get().trim();
                    http.authorizeRequests()
                        .antMatchers(method, cleanPattern.split(","))
                        .hasAnyAuthority(_roles);
                }
            }
        }

        //3. Prevent
        http.authorizeRequests().anyRequest().authenticated();
    }
}
