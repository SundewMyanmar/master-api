package com.sdm.file.service;

import com.sdm.core.config.properties.PathProperties;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.util.FileManager;
import com.sdm.core.util.Globalizer;
import com.sdm.file.model.File;
import com.sdm.file.repository.FileRepository;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class FileService {

    @Autowired
    FileRepository fileRepository;

    private final String fileUploadedPath;

    private final CacheControl cacheControl;

    @Autowired
    public FileService(PathProperties pathProperties) {
        this.fileUploadedPath = pathProperties.getUpload();
        this.cacheControl = CacheControl
                .maxAge(180, TimeUnit.DAYS)
                .cachePublic();
    }

    public File checkFile(String id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, "There is no file by : " + id));

        return file;
    }

    public void generatePreCacheImage(MultipartFile uploadFile, String storagePath, String ext) throws IOException {
        BufferedImage image = ImageIO.read(uploadFile.getInputStream());

        for (File.ImageSize size : File.ImageSize.values()) {
            String fileName = size.name().toLowerCase() + "." + ext;
            java.io.File saveFile = new java.io.File(storagePath + java.io.File.separator + fileName);
            log.info(String.format("Generating %s size image => %s", size, saveFile.getName()));
            if (image.getHeight() <= size.getMaxSize() && image.getWidth() <= size.getMaxSize()) {
                uploadFile.transferTo(saveFile);
                continue;
            }
            Double scale;
            if (image.getHeight() > image.getWidth()) {
                scale = Double.valueOf(size.getMaxSize()) / Double.valueOf(image.getHeight());
            } else {
                scale = Double.valueOf(size.getMaxSize()) / Double.valueOf(image.getWidth());
            }
            Thumbnails.of(image)
                    .scale(scale)
                    .useOriginalFormat()
                    .toFile(saveFile);
        }
    }

    public void remove(String id, boolean isTrash) {
        File fileEntity = this.checkFile(id);

        if (isTrash) {
            fileEntity.setStatus(File.Status.TRASH);
            fileRepository.save(fileEntity);
            return;
        }

        fileRepository.softDelete(fileEntity);
    }

    @Transactional
    public File create(MultipartFile uploadFile, boolean isPublic) {
        String fileName = StringUtils.cleanPath(uploadFile.getOriginalFilename());

        String[] nameInfo = FileManager.fileNameSplitter(fileName);

        File rawEntity = new File();
        rawEntity.setId(UUID.randomUUID().toString());
        rawEntity.setName(nameInfo[0]);
        if (nameInfo.length == 2) {
            rawEntity.setExtension(nameInfo[1]);
        }

        rawEntity.setPublicAccess(isPublic);
        if (StringUtils.isEmpty(uploadFile.getContentType())) {
            rawEntity.setType("application/octet-stream");
        } else {
            rawEntity.setType(uploadFile.getContentType());
        }

        rawEntity.setFileSize(uploadFile.getSize());
        rawEntity.setStatus(File.Status.STORAGE);

        String storagePath = Globalizer.getDateString("/yyyy/MM/", new Date());
        String storageName = rawEntity.getId();
        try {
            if (uploadFile.getContentType().contains("image")) {
                Path savePath = Paths.get(this.fileUploadedPath, storagePath, rawEntity.getId()).normalize();
                Files.createDirectories(savePath);
                generatePreCacheImage(uploadFile, savePath.toString(), rawEntity.getExtension());
                storageName += java.io.File.separator;
            } else {
                Path savePath = Paths.get(this.fileUploadedPath, storagePath).normalize();
                Files.createDirectories(savePath);
                java.io.File savedFile = new java.io.File(savePath + java.io.File.separator + storageName);
                uploadFile.transferTo(savedFile);
                storageName += "." + rawEntity.getExtension();
            }
        } catch (IOException ex) {
            log.warn(ex.getLocalizedMessage());
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
        rawEntity.setStoragePath(Paths.get(storagePath, storageName).normalize().toString());
        fileRepository.save(rawEntity);

        return rawEntity;
    }

    public ResponseEntity<?> downloadFile(String id, String fileName, File.ImageSize size, boolean isPublic) {
        File downloadEntity = this.checkFile(id);

        if (isPublic && !downloadEntity.isPublicAccess()) {
            throw new GeneralException(HttpStatus.UNAUTHORIZED, "You don't have access to this file.");
        }

        byte[] data;
        try {
            Path savedPath;
            // If it is image and include dimension, it will process image on dimension
            if (downloadEntity.getType().contains("image")) {
                String imageFile = size.name().toLowerCase() + "." + downloadEntity.getExtension();
                savedPath = Paths.get(this.fileUploadedPath, downloadEntity.getStoragePath(), imageFile).normalize();
            } else {
                savedPath = Paths.get(this.fileUploadedPath, downloadEntity.getStoragePath()).normalize();
            }
            data = Files.readAllBytes(savedPath);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }

        if (StringUtils.isEmpty(fileName)) {
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
