package com.sdm.master.controller;

import com.sdm.core.controller.ReadWriteController;
import com.sdm.core.model.response.ListModel;
import com.sdm.core.model.response.PaginationModel;
import com.sdm.core.repository.DefaultRepository;
import com.sdm.master.entity.MenuEntity;
import com.sdm.master.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/menus")
public class MenuController extends ReadWriteController<MenuEntity, Integer> {
    @Autowired
    private MenuRepository menuRepository;

    @Override
    protected DefaultRepository<MenuEntity, Integer> getRepository() {
        return menuRepository;
    }

    @RequestMapping(value = "/roles", method = RequestMethod.GET)
    public ResponseEntity getByRoles(@RequestParam(value = "ids", defaultValue = "0") String roles){
        try{
            String[] strRoles= roles.split(",") ;
            Integer[] intRoles = new Integer[strRoles.length];
            for (int i = 0; i < intRoles.length; i++){
                intRoles[i] = Integer.parseInt(strRoles[i]);
            }

            List<MenuEntity> results= menuRepository.findByRoles(intRoles);
            return new ResponseEntity(new ListModel(results), HttpStatus.OK);
        }catch(Exception ex){
            logger.error(ex.getLocalizedMessage(), ex);
            throw ex;
        }
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public ResponseEntity getPaging(@RequestParam(value = "filter", defaultValue = "") String filter,
                                    @RequestParam(value = "page", defaultValue = "0") int pageId,
                                    @RequestParam(value = "size", defaultValue = "10") int pageSize,
                                    @RequestParam(value = "sort", defaultValue = "id:DESC") String sortString) {
        try {
            Page<MenuEntity> paging = menuRepository.findByFilter(filter, this.buildPagination(pageId, pageSize, sortString));
            return new ResponseEntity(new PaginationModel<>(paging), HttpStatus.PARTIAL_CONTENT);
        }catch (Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
            throw ex;
        }
    }
}
