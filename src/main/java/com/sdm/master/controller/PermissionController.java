package com.sdm.master.controller;

import com.sdm.core.controller.ReadWriteController;
import com.sdm.core.model.response.PaginationModel;
import com.sdm.master.entity.PermissionEntity;
import com.sdm.master.repository.PermissionRepository;
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
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/permissions")
public class PermissionController extends ReadWriteController<PermissionEntity, Integer> {
    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    protected JpaRepository<PermissionEntity, Integer> getRepository() {
        return permissionRepository;
    }

    @Transactional
    @RequestMapping(value = "/paging", method = RequestMethod.GET)
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

            Page<PermissionEntity> paging = permissionRepository.findByFilter(filter,
                PageRequest.of(pageId, pageSize, Sort.by(sorting)));

            return new ResponseEntity(new PaginationModel<>(paging), HttpStatus.PARTIAL_CONTENT);
        } catch (Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
            throw ex;
        }
    }
}
