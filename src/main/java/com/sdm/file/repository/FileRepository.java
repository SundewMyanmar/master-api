package com.sdm.file.repository;

import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.file.model.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends DefaultRepository<File, String> {

    @Query("SELECT f FROM #{#entityName} f WHERE f.folder.id = :folderId")
    List<File> findAllByFolder(@Param("folderId") int folderId);

    @Query("SELECT f FROM #{#entityName} f WHERE f.folder.id = :folderId AND (:isPublic IS NULL OR f.publicAccess=:isPublic) AND (:isHidden IS NULL OR f.status=:isHidden) AND (LOWER(f.name) LIKE CONCAT('%',LOWER(:filter),'%'))")
    Page<File> findByFolder(Pageable paging, @Param("filter") String filter,@Param("folderId") int folderId,@Param("isPublic") Boolean isPublic,@Param("isHidden") File.Status isHidden);

    @Query("SELECT f FROM #{#entityName} f WHERE f.folder IS NULL AND (:isPublic IS NULL OR f.publicAccess=:isPublic) AND (:isHidden IS NULL OR f.status=:isHidden) AND (LOWER(f.name) LIKE CONCAT('%',LOWER(:filter),'%'))")
    Page<File> findByFolderIsNull(Pageable paging, @Param("filter") String filter,@Param("isPublic") Boolean isPublic,@Param("isHidden") File.Status isHidden);

    @Query("SELECT f FROM #{#entityName} f")
    Page<File> findTest(Pageable paging);
}
