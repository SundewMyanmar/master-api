package com.sdm.admin.controller;

import com.sdm.Constants;
import com.sdm.admin.model.SystemMenu;
import com.sdm.admin.repository.SystemMenuRepository;
import com.sdm.core.controller.DefaultReadWriteController;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.repository.DefaultRepository;
import com.sdm.core.util.Globalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/menus")
public class SystemMenuController extends DefaultReadWriteController<SystemMenu, Integer> {
    @Autowired
    private SystemMenuRepository systemMenuRepository;

    @Override
    protected DefaultRepository<SystemMenu, Integer> getRepository() {
        return systemMenuRepository;
    }

    @GetMapping("/tree")
    public ResponseEntity<ListResponse<SystemMenu>> getSystemMenuTree(@DefaultValue("") @RequestParam("filter") String filter) {
        var results = systemMenuRepository.findParentMenu(filter);
        return ResponseEntity.ok(new ListResponse<>(results));
    }

    @GetMapping("/me")
    public ResponseEntity<ListResponse<SystemMenu>> getMenuByCurrentUser() {
        List<Integer> roles = new ArrayList<>();
        getCurrentUser().getAuthorities().forEach(aut -> {
            String roleId = aut.getAuthority().replaceAll(Constants.Auth.AUTHORITY_PREFIX, "");
            roles.add(Globalizer.toInt(roleId, 0));
        });

        var results = systemMenuRepository.findByRoles(roles);
        return ResponseEntity.ok(new ListResponse<>(results));
    }
}
