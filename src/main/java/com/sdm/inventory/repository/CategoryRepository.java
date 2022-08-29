package com.sdm.inventory.repository;

import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.inventory.model.Category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends DefaultRepository<Category, Integer> {
    @Query("SELECT c FROM #{#entityName} c WHERE c.parent.id=:parent AND (LOWER(CONCAT(IFNULL(c.name,''),IFNULL(c.description,''))) LIKE LOWER(:filter))")
    Page<Category> getPagingByParent(Pageable pageable, @Param("filter") String filter, @Param("parent") Integer parent);
}
