// src/main/java/com/example/demoapp/controller/SoftwareController.java
package com.example.demoapp.controller;

import com.example.demoapp.model.Software;
import com.example.demoapp.repository.SoftwareRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://192.168.1.40:3000", allowCredentials = "true")
@RequestMapping("/api/software")
public class SoftwareController {

    @Value("${upload.location:/home/ubuntu/demo/uploads/}")
    private String uploadLocation;

    private final SoftwareRepository softwareRepository;

    public SoftwareController(SoftwareRepository softwareRepository) {
        this.softwareRepository = softwareRepository;
    }

    /**
     * Upload software: title, demo video, ZIP file, price
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadSoftware(
            @RequestParam("title") String title,
            @RequestParam("video") MultipartFile video,
            @RequestParam("zipFile") MultipartFile zipFile,
            @RequestParam("price") Double price) {

        // Validate input
        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Title is required");
        }
        if (video.isEmpty()) {
            return ResponseEntity.badRequest().body("Demo video is required");
        }
        if (zipFile.isEmpty()) {
            return ResponseEntity.badRequest().body("ZIP file is required");
        }
        if (price == null || price <= 0) {
            return ResponseEntity.badRequest().body("Valid price is required");
        }

        // Ensure upload directory exists
        File uploadDir = new File(uploadLocation);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (!created) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to create upload directory: " + uploadLocation);
            }
        }

        try {
            // Save video
            String videoFileName = System.currentTimeMillis() + "_" + sanitizeFilename(video.getOriginalFilename());
            Path videoPath = Paths.get(uploadLocation, videoFileName);
            Files.copy(video.getInputStream(), videoPath);
            System.out.println("✅ Video saved: " + videoPath);

            // Save ZIP
            String zipFileName = System.currentTimeMillis() + "_" + sanitizeFilename(zipFile.getOriginalFilename());
            Path zipPath = Paths.get(uploadLocation, zipFileName);
            Files.copy(zipFile.getInputStream(), zipPath);
            System.out.println("✅ ZIP saved: " + zipPath);

            // Save to DB
            Software software = new Software();
            software.setTitle(title.trim());
            software.setVideoUrl("/uploads/" + videoFileName);
            software.setZipUrl("/uploads/" + zipFileName);
            software.setPrice(price);

            Software saved = softwareRepository.save(software);
            return ResponseEntity.ok(saved);

        } catch (IOException e) {
            System.err.println("❌ File upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }

    /**
     * List all uploaded software
     */
    @GetMapping("/list")
    public List<Software> getAllSoftware() {
        System.out.println("📋 Fetching all software from DB");
        return softwareRepository.findAll();
    }

    /**
     * Sanitize filename to prevent path traversal
     */
    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
