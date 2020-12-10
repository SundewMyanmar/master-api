package com.sdm.file.repository;

import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.file.model.File;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends DefaultRepository<File, String> {

    @Query("SELECT f FROM #{#entityName} f WHERE f.folder.id = :folderId")
    List<File> findByFolder(@Param("folderId") int folderId);

    List<File> findByFolderIsNull();
}
