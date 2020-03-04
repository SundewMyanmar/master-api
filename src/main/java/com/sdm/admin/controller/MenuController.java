package com.sdm.admin.controller;

import com.sdm.admin.model.SystemMenu;
import com.sdm.admin.repository.MenuRepository;
import com.sdm.core.controller.DefaultReadWriteController;
import com.sdm.core.db.DefaultRepository;
import com.sdm.core.model.response.ListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/menus")
public class MenuController extends DefaultReadWriteController<SystemMenu, Integer> {
    @Autowired
    private MenuRepository menuRepository;

    @Override
    protected DefaultRepository<SystemMenu, Integer> getRepository() {
        return menuRepository;
    }

    @GetMapping("/tree")
    public ResponseEntity<ListResponse<SystemMenu>> getSystemMenuTree(@DefaultValue("") @RequestParam("filter") String filter) {
        var results = menuRepository.findParentMenu(filter);
        return ResponseEntity.ok(new ListResponse<>(results));
    }

    @RequestMapping(value = "/roles", method = RequestMethod.GET)
    public ResponseEntity<ListResponse<SystemMenu>> getByRoles(@RequestParam("ids") Integer[] roles) {
        var results = menuRepository.findByRoles(roles);
        return ResponseEntity.ok(new ListResponse<>(results));
    }
}
