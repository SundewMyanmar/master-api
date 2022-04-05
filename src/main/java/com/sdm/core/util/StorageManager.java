package com.sdm.core.util;

import com.sdm.core.model.annotation.FileClassification;

import org.springframework.web.multipart.MultipartFile;

public interface StorageManager {
    Object store(MultipartFile file, Integer folder, FileClassification info);
}
