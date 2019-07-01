package com.sdm.master.repository;

import com.sdm.core.repository.DefaultRepository;
import com.sdm.master.entity.FileEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends DefaultRepository<FileEntity, String> {

}
