package com.sdm.admin.controller;

import com.sdm.admin.model.SystemMenu;
import com.sdm.admin.repository.MenuRepository;
import com.sdm.core.controller.DefaultReadWriterController;
import com.sdm.core.db.DefaultRepository;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.PaginationResponse;
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
@RequestMapping("/admin/menus")
public class MenuController extends DefaultReadWriterController<SystemMenu, Integer> {
    @Autowired
    private MenuRepository menuRepository;

    @Override
    protected DefaultRepository<SystemMenu, Integer> getRepository() {
        return menuRepository;
    }

    @RequestMapping(value = "/roles", method = RequestMethod.GET)
    public ResponseEntity getByRoles(@RequestParam("ids") Integer[] roles) {
        List<SystemMenu> results = menuRepository.findByRoles(roles);
        return new ResponseEntity(new ListResponse<>(results), HttpStatus.OK);
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public ResponseEntity getPaging(@RequestParam(value = "filter", defaultValue = "") String filter,
                                    @RequestParam(value = "page", defaultValue = "0") int pageId,
                                    @RequestParam(value = "size", defaultValue = "10") int pageSize,
                                    @RequestParam(value = "sort", defaultValue = "id:DESC") String sortString) {
        Page<SystemMenu> paging = menuRepository.findByFilter(filter, this.buildPagination(pageId, pageSize, sortString));
        return new ResponseEntity(new PaginationResponse<>(paging), HttpStatus.PARTIAL_CONTENT);
    }
}
