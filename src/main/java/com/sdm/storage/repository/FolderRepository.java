package com.sdm.storage.repository;

import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.storage.model.Folder;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends DefaultRepository<Folder, Integer> {
    @Query("SELECT distinct f from #{#entityName} f WHERE f.parentId IS NULL AND (IFNULL(f.guild,'')=:guild) " +
            "AND lower(concat(COALESCE(f.name, ''),COALESCE(f.color, ''))) LIKE lower(concat(:filter, '%')) ORDER BY f.priority")
    List<Folder> findParentMenu(@Param("filter") String filter, @Param("guild") String guild);
}
