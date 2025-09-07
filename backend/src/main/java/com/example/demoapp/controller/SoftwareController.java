// src/main/java/com/example/demoapp/controller/SoftwareController.java
package com.example.demoapp.controller;

import com.example.demoapp.model.Software;
import com.example.demoapp.repository.SoftwareRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://192.168.1.40:3000", allowCredentials = "true")
@RequestMapping("/api/software")
public class SoftwareController {

    @Value("${upload.path:/home/ubuntu/demo/uploads/}")
    private String uploadPath;

    private final SoftwareRepository softwareRepository;

    public SoftwareController(SoftwareRepository softwareRepository) {
        this.softwareRepository = softwareRepository;
    }

    // Upload software (video + zip + metadata)
    @PostMapping("/upload")
    public Software uploadSoftware(
            @RequestParam("title") String title,
            @RequestParam("video") MultipartFile video,
            @RequestParam("zipFile") MultipartFile zipFile,
            @RequestParam("price") Double price) throws IOException {

        // Create upload directory
        new File(uploadPath).mkdirs();

        // Save video
        String videoFileName = System.currentTimeMillis() + "_" + video.getOriginalFilename();
        video.transferTo(Paths.get(uploadPath, videoFileName));

        // Save ZIP
        String zipFileName = System.currentTimeMillis() + "_" + zipFile.getOriginalFilename();
        zipFile.transferTo(Paths.get(uploadPath, zipFileName));

        // Save to DB
        Software software = new Software();
        software.setTitle(title);
        software.setVideoUrl("/uploads/" + videoFileName);
        software.setZipUrl("/uploads/" + zipFileName);
        software.setPrice(price);

        return softwareRepository.save(software);
    }

    // List all software
    @GetMapping("/list")
    public List<Software> getAllSoftware() {
        return softwareRepository.findAll();
    }
}
