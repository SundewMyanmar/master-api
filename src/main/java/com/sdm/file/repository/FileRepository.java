package com.sdm.file.repository;

import com.sdm.core.db.DefaultRepository;
import com.sdm.file.model.File;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends DefaultRepository<File, String> {

}
