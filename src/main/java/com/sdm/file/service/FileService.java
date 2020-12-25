package com.sdm.file.service;

import com.sdm.core.config.properties.PathProperties;
import com.sdm.core.exception.GeneralException;
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
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class FileService {

    public static final String[] SIZE_CODES = new String[]{"", "K", "M", "G", "T", "P", "E", "Z", "Y"};

    public static String byteSize(long size) {
        if (size <= 1024) {
            return "1 KB";
        }
        float resultSize = size;
        String result = resultSize + " T";
        for (String code : SIZE_CODES) {
            if (resultSize < 1024) {
                result = (Math.round(resultSize * 100.0) / 100.0) + " " + code;
                break;
            }
            resultSize /= 1024;
        }
        return result + "B";
    }

    public static String[] fileNameSplitter(String fileName) {
        String[] fileInfo = fileName.split("\\.(?=[^\\.]+$)");
        if (fileInfo.length < 2) {
            return new String[]{fileName};
        }
        return fileInfo;
    }


    @Autowired
    FileRepository fileRepository;

    private final String fileUploadedPath;

    private final CacheControl cacheControl;

    @Autowired
    public FileService(PathProperties pathProperties) {
        this.fileUploadedPath = pathProperties.getUpload();
        this.cacheControl = CacheControl
                .maxAge(365, TimeUnit.DAYS)
                .cachePublic();
    }

    public File checkFile(String id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, "There is no file by : " + id));
    }

    public void generatePreCacheImage(MultipartFile uploadFile, String storagePath, String ext) throws IOException {
        this.generatePreCacheImage(uploadFile.getInputStream(), storagePath, ext);
    }

    public void generatePreCacheImage(InputStream iStream, String storagePath, String ext) throws IOException {
        BufferedImage image = ImageIO.read(iStream);

        for (File.ImageSize size : File.ImageSize.values()) {
            String fileName = size.name().toLowerCase() + "." + ext;
            java.io.File saveFile = new java.io.File(storagePath + java.io.File.separator + fileName);
            log.info(String.format("Generating %s size image => %s", size, saveFile.getName()));
            if (size.getMaxSize() == 0) {
                ImageIO.write(image, ext, saveFile);
                continue;
            }

            Double scale;
            if (image.getHeight() <= size.getMaxSize() && image.getWidth() <= size.getMaxSize()) {
                scale = 1.0;
            } else if (image.getHeight() > image.getWidth()) {
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

    public String getExtension(String contentType) {
        String suffix = null;
        Iterator<ImageReader> readers =
                ImageIO.getImageReadersByMIMEType(contentType);
        while (suffix == null && readers.hasNext()) {
            ImageReaderSpi provider = readers.next().getOriginatingProvider();
            if (provider != null) {
                String[] suffixes = provider.getFileSuffixes();
                if (suffixes != null) {
                    suffix = suffixes[0];
                }
            }
        }
        return suffix;
    }

    @Transactional
    public File create(String urlString, boolean isPublic, boolean isHidden) throws IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        String contentType = conn.getContentType();
        String extension = this.getExtension(contentType);

        File rawEntity = new File();

        rawEntity.setId(UUID.randomUUID().toString());
        rawEntity.setName("Profile");
        rawEntity.setExtension(extension);
        rawEntity.setPublicAccess(isPublic);
        rawEntity.setType(contentType);
        rawEntity.setFileSize(conn.getContentLength());
        rawEntity.setStatus(File.Status.STORAGE);

        if(isHidden)rawEntity.setStatus(File.Status.HIDDEN);

        String storagePath = Globalizer.getDateString("/yyyy/MM/", new Date());
        String storageName = rawEntity.getId();
        try {
            Path savePath = Paths.get(this.fileUploadedPath, storagePath, rawEntity.getId()).normalize();
            Files.createDirectories(savePath);
            generatePreCacheImage(conn.getInputStream(), savePath.toString(), extension);
            storageName += java.io.File.separator;
        } catch (IOException ex) {
            log.warn(ex.getLocalizedMessage());
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }

        rawEntity.setStoragePath(Paths.get(storagePath, storageName).normalize().toString());
        fileRepository.save(rawEntity);
        return rawEntity;
    }

    @Transactional
    public File create(MultipartFile uploadFile, boolean isPublic, boolean isHidden) {
        String fileName = StringUtils.cleanPath(uploadFile.getOriginalFilename());

        String[] nameInfo = FileService.fileNameSplitter(fileName);

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
        if(isHidden)rawEntity.setStatus(File.Status.HIDDEN);

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
