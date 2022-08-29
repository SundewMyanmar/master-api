package com.sdm.storage.controller;

import com.sdm.storage.model.File;
import com.sdm.storage.service.FileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

import javax.validation.constraints.Size;

@Controller
@RequestMapping("/public")
public class PublicController {
    @Autowired
    private FileService fileService;

    @GetMapping("/files/{id}/{name}")
    public ResponseEntity<?> downloadFile(@PathVariable("id") @Size(max = 36, min = 36) String id,
                                          @PathVariable("name") String filename,
                                          @RequestParam("size") Optional<File.ImageSize> imageSize) {

        return fileService.downloadFile(id, filename, imageSize.orElse(File.ImageSize.medium), true);
    }
}
