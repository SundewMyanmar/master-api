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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/permissions")
public class PermissionController extends ReadWriteController<PermissionEntity, Integer> {
    @Autowired
    private PermissionRepository permissionRepository;

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
}
