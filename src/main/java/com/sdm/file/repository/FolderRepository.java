package com.sdm.file.repository;

import com.sdm.admin.model.SystemMenu;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.file.model.Folder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends DefaultRepository<Folder, Integer> {
    @Query("SELECT distinct f from #{#entityName} f WHERE f.parentId IS NULL " +
            "AND lower(concat(COALESCE(f.name, ''),COALESCE(f.color, ''))) LIKE lower(concat(:filter, '%')) ORDER BY f.priority")
    List<Folder> findParentMenu(String filter);
}
