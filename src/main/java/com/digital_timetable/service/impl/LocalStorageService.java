package com.digital_timetable.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LocalStorageService {

    private final String uploadDir = "uploads/"; // Directory where files will be saved

    // Constructor to ensure the upload directory exists
    public LocalStorageService() throws IOException {
        Path path = Paths.get(uploadDir);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    // Method to save the image and return the file name or path
    public String saveImageToLocalStorage(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir + fileName);
        Files.copy(file.getInputStream(), path);
        return fileName;  // You can return the file path or file name
    }
}
