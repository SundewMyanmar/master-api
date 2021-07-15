package com.sdm.file.controller;

import com.sdm.admin.model.SystemMenu;
import com.sdm.core.controller.DefaultReadWriteController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.file.model.File;
import com.sdm.file.model.Folder;
import com.sdm.file.repository.FileRepository;
import com.sdm.file.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.util.List;

@RestController
@RequestMapping("/folders")
public class FolderController extends DefaultReadWriteController<Folder, Integer> {
    @Autowired
    private FolderRepository repository;

    @Autowired
    private FileRepository fileRepository;

    @Override
    protected DefaultRepository<Folder, Integer> getRepository() {
        return this.repository;
    }

    @GetMapping("/root")
    public ResponseEntity<ListResponse<Folder>> getSystemMenuTree(@DefaultValue("") @RequestParam("filter") String filter) {
        var results = repository.findParentMenu(filter);
        return ResponseEntity.ok(new ListResponse<>(results));
    }

    @Transactional
    @Override
    public ResponseEntity<MessageResponse> remove(Integer integer) {
        //Move folder files to root folder by setting null
        List<File> files =fileRepository.findAllByFolder(integer);
        files.forEach(f->f.setFolder(null));
        fileRepository.saveAll(files);
        return super.remove(integer);
    }
}
