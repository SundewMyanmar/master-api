package com.sdm.admin.controller;

import com.sdm.admin.model.SystemRoute;
import com.sdm.admin.repository.PermissionRepository;
import com.sdm.core.controller.DefaultReadWriterController;
import com.sdm.core.db.DefaultRepository;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/admin/permissions")
public class PermissionController extends DefaultReadWriterController<SystemRoute, Integer> {
    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    public RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    protected DefaultRepository<SystemRoute, Integer> getRepository() {
        return permissionRepository;
    }

    @Transactional
    @PostMapping("/create")
    ResponseEntity customCreate(@Valid @RequestBody List<SystemRoute> request) {
        List<SystemRoute> result = new ArrayList<>();

        for (SystemRoute item : request) {
            SystemRoute entity = new SystemRoute();
            if (item.getId() != null && item.getId() > 0) {
                entity = permissionRepository.findById(item.getId()).get();
            } else {
                Optional<SystemRoute> dbEntity = permissionRepository.findByHttpMethodAndPattern(item.getHttpMethod(), item.getPattern());
                if (dbEntity.isPresent()) {
                    entity = dbEntity.get();
                }
            }
            entity.setPattern(item.getPattern());
            entity.setHttpMethod(item.getHttpMethod());

            if (item.getId() != null && item.getId() > 0 && !item.isChecked()) {
                //Role just remove or delete
                if (item.getRoles().size() <= 0)
                    permissionRepository.softDelete(entity);
                else
                    permissionRepository.save(entity);
            } else if (item.isChecked()) {
                result.add(permissionRepository.save(entity));
            }
        }
        return new ResponseEntity(new ListResponse<>(result), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public ResponseEntity getPaging(@RequestParam(value = "filter", defaultValue = "") String filter,
                                    @RequestParam(value = "page", defaultValue = "0") int pageId,
                                    @RequestParam(value = "size", defaultValue = "10") int pageSize,
                                    @RequestParam(value = "sort", defaultValue = "id:DESC") String sortString) {
        try {
            Page<SystemRoute> paging = permissionRepository.findByFilter(filter, this.buildPagination(pageId, pageSize, sortString));

            return new ResponseEntity(new PaginationResponse<>(paging), HttpStatus.PARTIAL_CONTENT);
        } catch (Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
            throw ex;
        }
    }

    @RequestMapping(value = "/{id}/role", method = RequestMethod.GET)
    public ResponseEntity getByRole(@PathVariable("id") Integer role) {
        try {
            Optional<List<SystemRoute>> results = permissionRepository.findByRoleId(role);
            if (!results.isPresent()) {
                throw new GeneralException(HttpStatus.NO_CONTENT,
                        "There is no any data by role : " + role.toString());
            }
            return new ResponseEntity(new ListResponse<>(results.get()), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
            throw ex;
        }
    }


    @RequestMapping(value = "/routes", method = RequestMethod.GET)
    public ResponseEntity getAllRoutes() {
        Map<RequestMappingInfo, HandlerMethod> requestMap = requestMappingHandlerMapping.getHandlerMethods();
        List<String> neglectController = Arrays.asList(new String[]{"org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController",
                "springfox.documentation.swagger.web.ApiResourceController", "com.sdm.core.controller.PublicController", "com.sdm.master.controller.AuthController",
                "com.sdm.master.controller.MasterPublicController"});
        Map<String, Map<String, Object>> resultMap = new HashMap<>();

        requestMap.keySet().stream().forEach(t -> {
                    HandlerMethod requestMethod = requestMap.get(t);
                    if (neglectController.contains(requestMethod.getBeanType().getName()))
                        return;

                    Map<String, Object> map = resultMap.get(requestMethod.getBeanType().getName());
                    if (map == null) {
                        map = new HashMap<>();
                        map.put("name", requestMethod.getBean().toString());
                    }

                    List<Map<String, Object>> mapList = (List<Map<String, Object>>) map.get("routes");
                    if (mapList == null) {
                        mapList = new ArrayList<>();
                    }

                    Map<String, Object> mapL = new HashMap<>();
                    mapL.put("name", requestMethod.getMethod().getName());
                    String method = (t.getMethodsCondition().getMethods().size() == 0 ? "GET" : t.getMethodsCondition().getMethods().toArray()[0]).toString();
                    mapL.put("method", method);
                    String pattern = (t.getPatternsCondition().getPatterns().size() == 0 ? "" : t.getPatternsCondition().getPatterns().toArray()[0].toString());
                    mapL.put("pattern", pattern);

                    mapList.add(mapL);
                    map.put("routes", mapList);

                    resultMap.put(requestMethod.getBeanType().getName(), map);
                }
        );

        return ResponseEntity.ok(resultMap);
    }
}
