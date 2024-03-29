package com.sdm.storage.controller;

import com.sdm.core.controller.DefaultReadWriteController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.storage.model.File;
import com.sdm.storage.model.Folder;
import com.sdm.storage.repository.FileRepository;
import com.sdm.storage.repository.FolderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ListResponse<Folder>> getSystemMenuTree(@DefaultValue("") @RequestParam("filter") String filter, @RequestParam(value = "guild", defaultValue = "") String guild) {
        var results = repository.findParentMenu(filter, guild);
        return ResponseEntity.ok(new ListResponse<>(results));
    }

    @Transactional
    @Override
    public ResponseEntity<MessageResponse> remove(Integer integer) {
        //Move folder files to root folder by setting null
        List<File> files = fileRepository.findAllByFolder(integer);
        files.forEach(f -> f.setFolder(null));
        fileRepository.saveAll(files);
        return super.remove(integer);
    }
}
