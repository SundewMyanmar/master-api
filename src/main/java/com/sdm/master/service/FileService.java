package com.sdm.master.service;

import com.sdm.core.config.PathProperties;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.util.FileManager;
import com.sdm.core.util.Globalizer;
import com.sdm.master.entity.FileEntity;
import com.sdm.master.repository.FileRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Autowired
    FileRepository fileRepository;

    private final String fileUploadedPath;

    @Autowired
    public FileService(PathProperties pathProperties) {
        this.fileUploadedPath = pathProperties.getUpload();
    }

    public FileEntity checkFile(String id) {
        FileEntity file = fileRepository.findById(id)
            .orElseThrow(() -> new GeneralException(HttpStatus.NO_CONTENT, "There is no file by : " + id));

        return file;
    }

    public FileEntity create(MultipartFile uploadFile, boolean isPublic) {
        String fileName = StringUtils.cleanPath(uploadFile.getOriginalFilename());

        String[] nameInfo = FileManager.fileNameSplitter(fileName);

        FileEntity rawEntity = new FileEntity();
        rawEntity.setId(UUID.randomUUID().toString());
        rawEntity.setName(nameInfo[0]);
        if (nameInfo.length == 2) {
            rawEntity.setExtension(nameInfo[1]);
        }

        rawEntity.setPublicAccess(isPublic);
        if (uploadFile.getContentType() == null || uploadFile.getContentType().isEmpty()) {
            rawEntity.setType("application/octet-stream");
        } else {
            rawEntity.setType(uploadFile.getContentType());
        }

        rawEntity.setFileSize(uploadFile.getSize());
        rawEntity.setStatus(FileEntity.Status.STORAGE);

        String storagePath = Globalizer.getDateString("/yyyy/MMMM/", new Date());
        String storageName = rawEntity.getId() + "." + rawEntity.getExtension();

        Path savePath = Paths.get(this.fileUploadedPath, storagePath).normalize();
        File savedFile = new File(savePath + "/" + storageName);
        try {
            Files.createDirectories(savePath);
            uploadFile.transferTo(savedFile);
        } catch (IOException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
        rawEntity.setStoragePath(Paths.get(storagePath, storageName).normalize().toString());
        fileRepository.save(rawEntity);

        return rawEntity;
    }

    public FileEntity modified(String id, MultipartFile uploadFile) {
        String fileName = StringUtils.cleanPath(uploadFile.getOriginalFilename());

        String[] nameInfo = FileManager.fileNameSplitter(fileName);

        FileEntity fileEntity = this.checkFile(id);
        fileEntity.setName(nameInfo[0]);

        Path savePath = Paths.get(this.fileUploadedPath, fileEntity.getStoragePath());
        try {
            uploadFile.transferTo(savePath.toFile());
        } catch (IOException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
        return fileEntity;
    }

    public void remove(String id, boolean isTrash) {
        FileEntity fileEntity = this.checkFile(id);

        if (isTrash) {
            fileEntity.setStatus(FileEntity.Status.TRASH);
            fileRepository.save(fileEntity);
            return;
        }

        Path savePath = Paths.get(this.fileUploadedPath, fileEntity.getStoragePath());
        try {
            Files.deleteIfExists(savePath);
            logger.info("Deleted File by ID => " + fileEntity.getId());
        } catch (IOException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
        fileRepository.delete(fileEntity);
    }

    public ResponseEntity downloadFile(String id, String fileName, Dimension dimension, boolean is64, boolean isPublic) {
        FileEntity downloadEntity = this.checkFile(id);

        if (isPublic && !downloadEntity.isPublicAccess()) {
            throw new GeneralException(HttpStatus.FORBIDDEN, "Sorry! you don't have permission.");
        }

        Path savedPath = Paths.get(this.fileUploadedPath, downloadEntity.getStoragePath());

        byte[] data;

        try {
            // If it is image and include dimension, it will process image on dimension
            if (downloadEntity.getType().contains("image") && dimension != null) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                Thumbnails.of(savedPath.toFile()).size(dimension.width, dimension.height).keepAspectRatio(true)
                    .useOriginalFormat().toOutputStream(output);
                data = output.toByteArray();
            } else {
                data = Files.readAllBytes(savedPath);
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }

        CacheControl cacheControl = CacheControl.maxAge(7, TimeUnit.DAYS);

        if (is64) {
            String base64String = Base64.getMimeEncoder().encodeToString(data);
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .cacheControl(cacheControl)
                .body(base64String);
        }

        if (fileName.isEmpty() || fileName.length() < 3) {
            fileName = downloadEntity.getName() + "." + downloadEntity.getExtension();
        } else if (!fileName.contains(".")) {
            fileName += "." + downloadEntity.getExtension();
        }

        String attachment = "attachment; filename=\"" + fileName + "\"";

        Resource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(downloadEntity.getType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, attachment)
            .cacheControl(cacheControl)
            .body(resource);
    }
}
