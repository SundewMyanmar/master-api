package com.sdm.core.controller;

import com.sdm.core.model.AuthInfo;
import com.sdm.core.util.Globalizer;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class DefaultController {

    protected AuthInfo getCurrentUser() {
        return (AuthInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    protected Pageable buildPagination(int pageId, int pageSize, String sortString) {
        sortString = sortString.replaceAll("\\s+", "");
        List<Sort.Order> sorting = new ArrayList<>();
        if (!Globalizer.isNullOrEmpty(sortString)) {
            String[] sorts = sortString.split(",");
            for (String sort : sorts) {
                String[] sortParams = sort.strip().split(":", 2);
                if (sortParams.length >= 2 && sortParams[1].equalsIgnoreCase("desc")) {
                    sorting.add(Sort.Order.desc(sortParams[0]));
                } else {
                    sorting.add(Sort.Order.asc(sortParams[0]));
                }
            }
        }
        return PageRequest.of(pageId, pageSize, Sort.by(sorting));
    }
}
