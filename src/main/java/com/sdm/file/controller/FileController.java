package com.sdm.file.controller;

import com.sdm.core.controller.DefaultReadController;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.repository.DefaultRepository;
import com.sdm.file.model.File;
import com.sdm.file.repository.FileRepository;
import com.sdm.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/files")
public class FileController extends DefaultReadController<File, String> {

    @Autowired
    private FileService fileService;

    @Autowired
    private FileRepository fileRepository;

    @Override
    protected DefaultRepository<File, String> getRepository() {
        return this.fileRepository;
    }

    @DeleteMapping("{id}")
    public ResponseEntity<MessageResponse> remove(@PathVariable("id") String id,
                                                  @RequestParam(value = "isTrash", required = false, defaultValue = "false") boolean isTrash) {
        fileService.remove(id, isTrash);
        MessageResponse message = new MessageResponse(HttpStatus.OK, "Successfully deleted.",
                "Deleted data on your request by : " + id, null);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("")
    @Transactional
    public ResponseEntity<MessageResponse> multiRemove(@RequestBody Set<String> ids,
                                                       @RequestParam(value = "isTrash", required = false, defaultValue = "false") final boolean isTrash) {
        ids.forEach(id -> fileService.remove(id, isTrash));
        MessageResponse message = new MessageResponse(HttpStatus.OK, "Successfully deleted.",
                "Deleted data on your request.", null);
        return ResponseEntity.ok(message);
    }

    @PostMapping("upload")
    @Transactional
    public ResponseEntity<ListResponse<File>> uploadFile(@RequestParam("uploadedFile") List<MultipartFile> files,
                                                         @RequestParam(value = "isPublic", required = false, defaultValue = "false") boolean isPublic) {
        ListResponse<File> uploadedFiles = new ListResponse<>();
        files.forEach(file -> {
            File fileEntity = fileService.create(file, isPublic);
            uploadedFiles.addData(fileEntity);
        });
        return new ResponseEntity(uploadedFiles, HttpStatus.CREATED);
    }


    @GetMapping("download/{id}/{fileName}")
    public ResponseEntity<?> downloadFile(@PathVariable("id") String id,
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
