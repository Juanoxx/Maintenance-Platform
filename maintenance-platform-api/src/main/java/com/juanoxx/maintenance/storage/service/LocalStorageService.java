package com.juanoxx.maintenance.storage.service;

import com.juanoxx.maintenance.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalStorageService {

    private final StorageProperties storageProperties;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Path.of(storageProperties.getUploadDir()));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to initialize upload directory", ex);
        }
    }

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "File is required");
        }
        String extension = extractExtension(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + extension;
        Path destination = Path.of(storageProperties.getUploadDir()).resolve(storedName);
        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file");
        }

        return new StoredFile(
                file.getOriginalFilename(),
                storedName,
                destination.toString(),
                file.getContentType() == null ? "application/octet-stream" : file.getContentType(),
                file.getSize()
        );
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    public record StoredFile(
            String originalName,
            String storedName,
            String storagePath,
            String mimeType,
            long sizeBytes
    ) {
    }
}
