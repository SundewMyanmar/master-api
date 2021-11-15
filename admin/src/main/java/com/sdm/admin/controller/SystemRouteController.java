package com.sdm.admin.controller;

import com.sdm.admin.model.Role;
import com.sdm.admin.model.SystemRoute;
import com.sdm.admin.repository.RoleRepository;
import com.sdm.admin.repository.SystemRouteRepository;
import com.sdm.core.controller.DefaultReadController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.ListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@RestController
@RequestMapping("/admin/routes")
public class SystemRouteController extends DefaultReadController<SystemRoute, Integer> {
    @Autowired
    private SystemRouteRepository systemRouteRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    protected DefaultRepository<SystemRoute, Integer> getRepository() {
        return systemRouteRepository;
    }

    @RequestMapping(value = "/role/{id}", method = RequestMethod.GET)
    public ResponseEntity<ListResponse<SystemRoute>> getByRole(@PathVariable("id") Integer role) {
        var results = systemRouteRepository.findByRoleId(role)
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE,
                        localeManager.getMessage("no-data-by", role)));

        return ResponseEntity.ok(new ListResponse<>(results));
    }

    @Transactional
    @RequestMapping(value = "/role/{id}", method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<ListResponse<SystemRoute>> savePermission(@PathVariable("id") Integer roleId, @RequestBody @Valid List<SystemRoute> request) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE,
                localeManager.getMessage("no-data-by", roleId)));
        List<SystemRoute> savedRoutes = new ArrayList<>();
        systemRouteRepository.clearPermissionByRoleId(roleId);

        Consumer<SystemRoute> addRole = (entityRoute) -> {
            entityRoute.setPattern(entityRoute.getSqlPattern());
            entityRoute.addRole(role);
            entityRoute = systemRouteRepository.save(entityRoute);
            savedRoutes.add(entityRoute);
        };

        for (SystemRoute route : request) {
            systemRouteRepository.findById(route.getId())
                    .ifPresentOrElse(addRole, () -> {
                        SystemRoute dbRoute = systemRouteRepository
                                .findFirstByHttpMethodAndPattern(route.getHttpMethod(), route.getSqlPattern())
                                .orElse(route);
                        addRole.accept(dbRoute);
                    });
        }

        return new ResponseEntity<>(new ListResponse<>(savedRoutes), HttpStatus.CREATED);
    }
}
