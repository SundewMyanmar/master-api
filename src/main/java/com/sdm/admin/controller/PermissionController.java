package com.sdm.admin.controller;

import com.sdm.admin.model.SystemRoute;
import com.sdm.admin.repository.SystemRouteRepository;
import com.sdm.core.controller.DefaultReadWriteController;
import com.sdm.core.db.DefaultRepository;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.ListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/admin/permissions")
public class PermissionController extends DefaultReadWriteController<SystemRoute, Integer> {
    @Autowired
    private SystemRouteRepository systemRouteRepository;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    protected DefaultRepository<SystemRoute, Integer> getRepository() {
        return systemRouteRepository;
    }

    @Override
    @Transactional
    public ResponseEntity<ListResponse<SystemRoute>> multiCreate(@Valid List<SystemRoute> request) {
        var result = new ArrayList<SystemRoute>();

        for (var item : request) {
            SystemRoute entity = new SystemRoute();
            if (item.getId() != null && item.getId() > 0) {
                entity = systemRouteRepository.findById(item.getId()).get();
            } else {
                Optional<SystemRoute> dbEntity = systemRouteRepository.findOneByHttpMethodAndPattern(item.getHttpMethod(), item.getPattern());
                if (dbEntity.isPresent()) {
                    entity = dbEntity.get();
                }
            }
            entity.setPattern(item.getPattern());
            entity.setHttpMethod(item.getHttpMethod());

            if (item.getId() != null && item.getId() > 0 && !item.isChecked()) {
                //Role just remove or delete
                if (item.getRoles().size() <= 0)
                    systemRouteRepository.softDelete(entity);
                else
                    systemRouteRepository.save(entity);
            } else if (item.isChecked()) {
                result.add(systemRouteRepository.save(entity));
            }
        }
        return new ResponseEntity<>(new ListResponse<>(result), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{id}/role", method = RequestMethod.GET)
    public ResponseEntity<ListResponse<SystemRoute>> getByRole(@PathVariable("id") Integer role) {
        var results = systemRouteRepository.findByRoleId(role)
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE,
                        "There is no any data by role : " + role.toString()));

        return ResponseEntity.ok(new ListResponse<>(results));
    }


    @RequestMapping(value = "/routes", method = RequestMethod.GET)
    public ResponseEntity<?> getAllRoutes() {
        var requestMap = requestMappingHandlerMapping.getHandlerMethods();
        var neglectController = List.of(
                "org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController",
                "springfox.documentation.swagger.web.ApiResourceController",
                "com.sdm.core.controller.RootController",
                "com.sdm.auth.controller.AuthController",
                "com.sdm.file.controller.PublicController"
        );
        var resultMap = new HashMap<String, Map<String, Object>>();

        requestMap.keySet().forEach(t -> {
            HandlerMethod requestMethod = requestMap.get(t);
            if (neglectController.contains(requestMethod.getBeanType().getName()))
                return;

            var map = resultMap.getOrDefault(requestMethod.getBeanType().getName(),
                    Map.of("name", requestMethod.getBean().toString()));


            var mapList = (List<Map<String, String>>) map.getOrDefault("routes", new ArrayList<>());
            String method = (t.getMethodsCondition().getMethods().size() == 0 ? "GET" : t.getMethodsCondition().getMethods().toArray()[0]).toString();
            String pattern = (t.getPatternsCondition().getPatterns().size() == 0 ? "" : t.getPatternsCondition().getPatterns().toArray()[0].toString());
            var mapL = Map.of(
                    "name", requestMethod.getMethod().getName(),
                    "method", method,
                    "pattern", pattern
            );
            mapList.add(mapL);
            map.put("routes", map);

            resultMap.put(requestMethod.getBeanType().getName(), map);
        });

        return ResponseEntity.ok(resultMap);
    }
}
