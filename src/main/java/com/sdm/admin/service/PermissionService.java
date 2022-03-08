package com.sdm.admin.service;

import com.sdm.admin.model.SystemRoute;
import com.sdm.admin.repository.SystemRouteRepository;
import com.sdm.core.Constants;
import com.sdm.core.security.PermissionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service("permissionHandler")
public class PermissionService implements PermissionHandler {

    @Autowired
    SystemRouteRepository routeRepository;

    @Override
    public boolean check(Authentication authentication, HttpServletRequest request) {
        String pattern = request.getRequestURI();
        String method = request.getMethod();

        List<SystemRoute> dbRoutes = routeRepository.checkPermissionRequest(method, pattern).orElse(new ArrayList<>());
        List<SystemRoute> exactRoutes = dbRoutes.stream().filter(r -> r.getPattern().equalsIgnoreCase(pattern)).collect(Collectors.toList());
        List<SystemRoute> routes = exactRoutes.size() > 0 ? exactRoutes : dbRoutes;
        Collections.sort(routes);

        for (GrantedAuthority aut : authentication.getAuthorities()) {
            //Check Root Role
            if (aut.getAuthority().equals(Constants.Auth.ROOT_ROLE)) {
                return true;
            }

            //Check DB Permission
            if (routes.size() > 0) {
                for (SystemRoute route : routes) {
                    if (route.checkPermission(aut)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
