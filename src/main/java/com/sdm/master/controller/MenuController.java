package com.sdm.master.controller;

import com.sdm.core.controller.ReadWriteController;
import com.sdm.core.model.response.ListModel;
import com.sdm.core.model.response.PaginationModel;
import com.sdm.master.entity.MenuEntity;
import com.sdm.master.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/menus")
public class MenuController extends ReadWriteController<MenuEntity, Integer> {
    @Autowired
    private MenuRepository menuRepository;

    @Override
    protected JpaRepository<MenuEntity, Integer> getRepository() {
        return menuRepository;
    }

    @Transactional
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

    @Transactional
    @RequestMapping(value="/paging",method = RequestMethod.GET)
    public ResponseEntity getPaging(@RequestParam(value = "filter", defaultValue = "") String filter,
                                    @RequestParam(value = "page", defaultValue = "0") int pageId,
                                    @RequestParam(value = "size", defaultValue = "10") int pageSize,
                                    @RequestParam(value = "sort", defaultValue = "id:DESC") String sortString) {
        try {
            List<Sort.Order> sorting = new ArrayList<>();
            if (sortString.length() > 0) {
                String[] sorts = sortString.split(",");
                for (String sort : sorts) {
                    String[] sortParams = sort.trim().split(":", 2);
                    if (sortParams.length >= 2 && sortParams[1].equalsIgnoreCase("desc")) {
                        sorting.add(Sort.Order.desc(sortParams[0]));
                    } else {
                        sorting.add(Sort.Order.asc(sortParams[0]));
                    }
                }
            }

            Page<MenuEntity> paging=menuRepository.findByFilter(filter, PageRequest.of(pageId, pageSize, Sort.by(sorting)));

            return new ResponseEntity(new PaginationModel<>(paging), HttpStatus.PARTIAL_CONTENT);
        }catch (Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
            throw ex;
        }
    }
}
