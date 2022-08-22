package com.sdm.admin.controller;

import com.sdm.admin.model.Role;
import com.sdm.admin.repository.RoleRepository;
import com.sdm.admin.service.ReactiveRoleService;
import com.sdm.core.controller.DefaultReadWriteController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.core.model.response.PaginationResponse;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/reactive/roles")
public class ReactiveRoleController extends DefaultReadWriteController<Role, Integer> {
    @Autowired
    private RoleRepository repository;

    @Override
    protected DefaultRepository<Role, Integer> getRepository() {
        return repository;
    }

    @Autowired
    private ReactiveRoleService service;

    @GetMapping("/{id}")
    private Publisher<ResponseEntity<Role>> getRoleById(@PathVariable Integer id) {
        return service.getHttpRoleById(id);
    }

    @GetMapping("/all")
    private Publisher<ResponseEntity<Role>> getAllRoles(){
        return service.getHttpFindAll();
    }

    @GetMapping("/paging")
    private Publisher<ResponseEntity<PaginationResponse<Role>>> getPaging(@RequestParam(value = "page", defaultValue = "0") int page,
                                                                           @RequestParam(value = "size", defaultValue = "10") int pageSize,
                                                                           @RequestParam(value = "filter", defaultValue = "") String filter,
                                                                           @RequestParam(value = "sort", defaultValue = "id:DESC") String sort){
        return service.paging(this.buildPagination(page, pageSize, sort), "%" + filter.toLowerCase(Locale.ROOT) + "%");
    }
}
