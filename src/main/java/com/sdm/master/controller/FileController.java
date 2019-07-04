package com.sdm.master.controller;

import com.sdm.core.controller.ReadController;
import com.sdm.core.model.response.ListModel;
import com.sdm.core.model.response.MessageModel;
import com.sdm.core.repository.DefaultRepository;
import com.sdm.master.entity.FileEntity;
import com.sdm.master.repository.FileRepository;
import com.sdm.master.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/files/")
public class FileController extends ReadController<FileEntity, String> {

    @Autowired
    private FileService fileService;

    @Autowired
    private FileRepository fileRepository;

    @Override
    protected DefaultRepository<FileEntity, String> getRepository() {
        return this.fileRepository;
    }

    @DeleteMapping("{id}")
    public ResponseEntity remove(@PathVariable("id") String id,
                                 @RequestParam(value = "isTrash", required = false, defaultValue = "false") boolean isTrash) {
        fileService.remove(id, isTrash);
        MessageModel message = MessageModel.createMessage("Successfully deleted.",
                "Deleted data on your request by : " + id);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/multi")
    @Transactional
    ResponseEntity multiRemove(@RequestBody Set<String> ids,
                               @RequestParam(value = "isTrash", required = false, defaultValue = "false") final boolean isTrash) {
        ids.forEach(id -> fileService.remove(id, isTrash));
        MessageModel message = MessageModel.createMessage("Successfully deleted.",
                "Deleted data on your request.");
        return ResponseEntity.ok(message);
    }

    @PostMapping("upload")
    public ResponseEntity uploadFile(@RequestParam("uploadedFile") MultipartFile file,
                                     @RequestParam(value = "isPublic", required = false, defaultValue = "true") boolean isPublic) {
        FileEntity fileEntity = fileService.create(file, isPublic);
        return new ResponseEntity(fileEntity, HttpStatus.CREATED);
    }

    @PostMapping("multi/upload")
    public ResponseEntity uploadMultipleFiles(@RequestParam("uploadedFile") MultipartFile[] files,
                                              @RequestParam(value = "isPublic", required = false, defaultValue = "true") boolean isPublic) {
        List<FileEntity> uploadedFiles = Arrays.asList(files)
                .stream()
                .map(file -> fileService.create(file, isPublic))
                .collect(Collectors.toList());

        return new ResponseEntity(new ListModel<>(uploadedFiles), HttpStatus.CREATED);
    }

    @PutMapping("upload/{id}")
    public ResponseEntity uploadFile(@PathVariable("id") String id,
                                     @RequestParam("uploadedFile") MultipartFile file) {
        FileEntity fileEntity = fileService.modified(id, file);
        return new ResponseEntity(fileEntity, HttpStatus.ACCEPTED);
    }

    @GetMapping("download/{id}/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") String id,
                                                 @PathVariable("fileName") String fileName,
                                                 @RequestParam(value = "width", required = false, defaultValue = "0") int width,
                                                 @RequestParam(value = "height", required = false, defaultValue = "0") int height,
                                                 @RequestParam(value = "is64", required = false, defaultValue = "false") boolean is64) {

        Dimension dimension = null;
        if (width > 0 && height <= 0) {
            dimension = new Dimension(width, width);
        } else if (height > 0 && width <= 0) {
            dimension = new Dimension(height, height);
        } else if (width > 0 && height > 0) {
            dimension = new Dimension(width, height);
        }

        return fileService.downloadFile(id, fileName, dimension, is64, false);
    }
}
