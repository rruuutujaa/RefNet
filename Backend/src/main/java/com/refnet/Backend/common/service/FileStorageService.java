package com.refnet.Backend.common.service;

import com.refnet.Backend.common.exception.StorageException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.resume-subdir:resumes}")
    private String resumeSubDir;

    @Value("${file.photo-subdir:photos}")
    private String photoSubDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir, resumeSubDir).toAbsolutePath().normalize());
            Files.createDirectories(Paths.get(uploadDir, photoSubDir).toAbsolutePath().normalize());
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage directory", e);
        }
    }

    public String storeResume(MultipartFile file) {
        validateResume(file);
        return storeFile(file, resumeSubDir);
    }

    public String storePhoto(MultipartFile file) {
        validatePhoto(file);
        return storeFile(file, photoSubDir);
    }

    private String storeFile(MultipartFile file, String subDir) {
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        
        // Path Traversal Security Check
        if (originalFileName.contains("..")) {
            throw new StorageException("Cannot store file with relative path outside current directory " + originalFileName);
        }

        try {
            String extension = StringUtils.getFilenameExtension(originalFileName);
            String fileName = UUID.randomUUID().toString() + (extension != null ? "." + extension : "");
            
            Path targetLocation = Paths.get(uploadDir, subDir).toAbsolutePath().normalize().resolve(fileName);
            
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return "/" + subDir + "/" + fileName;
        } catch (IOException e) {
            throw new StorageException("Failed to store file", e);
        }
    }

    private void validateResume(MultipartFile file) {
        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            throw new StorageException("Invalid file type. Only PDF is allowed for resumes.");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new StorageException("File size exceeds 5MB limit.");
        }
    }

    private void validatePhoto(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new StorageException("Invalid file type. Only JPG/PNG are allowed for photos.");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new StorageException("File size exceeds 2MB limit.");
        }
    }
}
