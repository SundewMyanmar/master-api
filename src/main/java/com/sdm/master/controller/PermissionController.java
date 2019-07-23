package com.sdm.master.controller;

import com.sdm.core.controller.ReadWriteController;
import com.sdm.core.model.response.PaginationModel;
import com.sdm.core.repository.DefaultRepository;
import com.sdm.master.entity.PermissionEntity;
import com.sdm.master.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/permissions")
public class PermissionController extends ReadWriteController<PermissionEntity, Integer> {
    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    public RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    protected DefaultRepository<PermissionEntity, Integer> getRepository() {
        return permissionRepository;
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public ResponseEntity getPaging(@RequestParam(value = "filter", defaultValue = "") String filter,
                                    @RequestParam(value = "page", defaultValue = "0") int pageId,
                                    @RequestParam(value = "size", defaultValue = "10") int pageSize,
                                    @RequestParam(value = "sort", defaultValue = "id:DESC") String sortString) {
        try {
            Page<PermissionEntity> paging = permissionRepository.findByFilter(filter, this.buildPagination(pageId, pageSize, sortString));

            return new ResponseEntity(new PaginationModel<>(paging), HttpStatus.PARTIAL_CONTENT);
        } catch (Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
            throw ex;
        }
    }

    @RequestMapping(value = "/routes", method=RequestMethod.GET)
    public @ResponseBody Object showEndpointsAction() {
        Map<RequestMappingInfo, HandlerMethod> requestMap= requestMappingHandlerMapping.getHandlerMethods();
        List<String> neglectController= Arrays.asList(new String[]{"org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController",
                "springfox.documentation.swagger.web.ApiResourceController"});
        Map<String,Map<String,Object>> resultMap=new HashMap<>();

        requestMap.keySet().stream().forEach(t->{
                    HandlerMethod requestMethod=requestMap.get(t);
                    if(neglectController.contains(requestMethod.getBeanType().getName()))
                        return;

                    Map<String,Object> map=resultMap.get(requestMethod.getBeanType().getName());
                    if(map==null){
                        map=new HashMap<>();
                        map.put("name",requestMethod.getBean().toString());
                    }

                    List<Map<String,Object>> mapList=(List<Map<String,Object>>)map.get("routes");
                    if(mapList==null){
                        mapList=new ArrayList<>();
                    }

                    Map<String,Object> mapL=new HashMap<>();
                    mapL.put("name",requestMethod.getMethod().getName());
                    String method=(t.getMethodsCondition().getMethods().size() == 0 ? "GET" : t.getMethodsCondition().getMethods().toArray()[0]).toString();
                    mapL.put("method",method);
                    String pattern=(t.getPatternsCondition().getPatterns().size()==0?"":t.getPatternsCondition().getPatterns().toArray()[0].toString());
                    mapL.put("pattern",pattern);

                    mapList.add(mapL);
                    map.put("routes",mapList);

                    resultMap.put(requestMethod.getBeanType().getName(),map);
                }
        );

        return resultMap;
    }
}
