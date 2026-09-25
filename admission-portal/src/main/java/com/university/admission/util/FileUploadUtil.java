package com.university.admission.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Xu ly luu file minh chung (hoc ba, CCCD...) len o dia cuc bo.
 * Duong dan luu duoc cau hinh qua app.upload.dir trong application.properties.
 */
@Component
public class FileUploadUtil {

    @Value("${app.upload.dir:uploads/documents}")
    private String uploadDir;

    /**
     * Luu file va tra ve duong dan tuong doi da luu (dung de ghi vao CSDL cot file_path).
     */
    public String saveFile(MultipartFile file, Long applicationId) throws IOException {
        Path dirPath = Paths.get(uploadDir, String.valueOf(applicationId));
        Files.createDirectories(dirPath);

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID() + extension;

        Path targetPath = dirPath.resolve(storedName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath.toString();
    }
}
