package com.sdm.storage.service;

import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.annotation.FileClassification;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.LocaleManager;
import com.sdm.core.util.StorageManager;
import com.sdm.storage.model.File;
import com.sdm.storage.repository.FileRepository;
import com.sdm.storage.repository.FolderRepository;

import lombok.extern.log4j.Log4j2;

import net.coobird.thumbnailator.Thumbnails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class FileService implements StorageManager {

    @Value("${com.sdm.path.upload:/var/www/master-api/upload/}")
    private String uploadRootPath;

    public static final String[] SIZE_CODES = new String[]{"", "K", "M", "G", "T", "P", "E", "Z", "Y"};

    private final CacheControl cacheControl;

    @Autowired
    FileRepository fileRepository;

    @Autowired
    FolderRepository folderRepository;

    @Autowired
    LocaleManager localeManager;

    public FileClassification getInstanceFileClassification(final String guild, final boolean isPublic, final boolean isHidden){
        return new FileClassification(){
            @Override
            public Class<? extends Annotation> annotationType() {
                return FileClassification.class;
            }

            @Override
            public String guild() {
                return guild;
            }

            @Override
            public boolean isHidden() {
                return isHidden;
            }

            @Override
            public boolean isPublic() {
                return isPublic;
            }
        };
    }

    @Autowired
    public FileService() {
        this.cacheControl = CacheControl
                .maxAge(365, TimeUnit.DAYS)
                .cachePublic();
    }

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

    public File checkFile(String id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, localeManager.getMessage("no-data-by", id)));
    }

    public void generatePreCacheImage(InputStream iStream, String storagePath, String ext) throws IOException {
        BufferedImage image = ImageIO.read(iStream);

        for (File.ImageSize size : File.ImageSize.values()) {
            String fileName = size.name().toLowerCase() + "." + ext;
            java.io.File saveFile =  Path.of(storagePath, fileName).normalize().toFile();
            log.info(String.format("Generating %s size image => %s", size, saveFile.getName()));
            if (size.getMaxSize() == 0) {
                ImageIO.write(image, ext, saveFile);
                continue;
            }

            double scale;
            if (image.getHeight() <= size.getMaxSize() && image.getWidth() <= size.getMaxSize()) {
                scale = 1.0;
            } else if (image.getHeight() > image.getWidth()) {
                scale = (double) size.getMaxSize() / (double) image.getHeight();
            } else {
                scale = (double) size.getMaxSize() / (double) image.getWidth();
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

    public File buildEntity(FileClassification fileClassification, String name, String extension, String contentType, long size) throws IOException {
        File fileEntity = new File();
        fileEntity.setId(UUID.randomUUID().toString());

        boolean isPublic = false;
        boolean isHidden = false;
        String guild = "";
        if (fileClassification != null) {
            isPublic = fileClassification.isPublic();
            isHidden = fileClassification.isHidden();
            guild = fileClassification.guild();
        }

        fileEntity.setPublicAccess(isPublic);
        fileEntity.setGuild(guild);
        fileEntity.setStatus(File.Status.STORAGE);
        fileEntity.setName(name);
        fileEntity.setExtension(extension);
        fileEntity.setType(contentType);
        fileEntity.setFileSize(size);

        if (isHidden) fileEntity.setStatus(File.Status.HIDDEN);

        return fileEntity;
    }

    public void fileProcessing(InputStream inputStream, File fileEntity) throws IOException {
        String storagePath = Globalizer.getDateString("/yyyy/MM/", new Date());
        Path savePath = Paths.get(uploadRootPath, storagePath, fileEntity.getId()).normalize();
        if(!Files.exists(savePath)){
            Files.createDirectories(savePath);
        }
        if (Objects.requireNonNull(fileEntity.getType()).contains("image")) {
            generatePreCacheImage(inputStream, savePath.toString(), fileEntity.getExtension());
            fileEntity.setStoragePath(Paths.get(storagePath, fileEntity.getId()).normalize().toString());
        } else {
            String fileName = fileEntity.getName() + "." + fileEntity.getExtension();
            java.io.File saveFile = Paths.get(savePath.toString(), fileName).normalize().toFile();
            FileCopyUtils.copy(inputStream, new FileOutputStream(saveFile));
            fileEntity.setStoragePath(saveFile.getPath());
        }
        log.info("Save uploaded file => " + fileEntity.getStoragePath());
    }

    @Transactional
    public File loadExternalImage(String urlString, FileClassification fileClassification) throws IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        String contentType = conn.getContentType();
        String extension = this.getExtension(contentType);
        File rawEntity = this.buildEntity(fileClassification, "Profile", extension, contentType, conn.getContentLength());
        this.fileProcessing(conn.getInputStream(), rawEntity);

        fileRepository.save(rawEntity);
        return rawEntity;
    }

    @Transactional
    public File create(MultipartFile uploadFile, Integer folderId, FileClassification fileClassification) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(uploadFile.getOriginalFilename()));

        String[] nameInfo = FileService.fileNameSplitter(fileName);

        String name = nameInfo[0];
        String ext = "";
        String contentType = "application/octet-stream";
        if (nameInfo.length == 2) {
            ext = nameInfo[1];
        }

        if(!Globalizer.isNullOrEmpty(uploadFile.getContentType())){
            contentType = uploadFile.getContentType();
        }

        try {
            File rawEntity = this.buildEntity(fileClassification, name, ext, contentType, uploadFile.getSize());
            this.fileProcessing(uploadFile.getInputStream(), rawEntity);

            if (folderId != null)
                folderRepository.findById(folderId).ifPresent(rawEntity::setFolder);

            fileRepository.save(rawEntity);
            return rawEntity;

        }catch(IOException ex){
            log.warn(ex.getLocalizedMessage());
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    public Object store(MultipartFile uploadFile, Integer folderId, FileClassification fileClassification) {
        return this.create(uploadFile, folderId, fileClassification);
    }

    public ResponseEntity<?> downloadFile(String id, String fileName, File.ImageSize size, boolean isPublic) {
        File downloadEntity = this.checkFile(id);

        if (isPublic && !downloadEntity.isPublicAccess()) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("access-denied"));
        }

        byte[] data;
        try {
            Path savedPath;
            //Independent File Separator
            String storagePath = downloadEntity.getStoragePath();//.replaceAll("[\\\\/]", java.io.File.separator);
            // If it is image and include dimension, it will process image on dimension
            if (downloadEntity.getType().contains("image")) {
                String imageFile = size.name().toLowerCase() + "." + downloadEntity.getExtension();
                savedPath = Paths.get(uploadRootPath, storagePath, imageFile).normalize();
            } else {
                savedPath = Paths.get(uploadRootPath, storagePath).normalize();
            }
            data = Files.readAllBytes(savedPath);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }

        if (Globalizer.isNullOrEmpty(fileName)) {
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
