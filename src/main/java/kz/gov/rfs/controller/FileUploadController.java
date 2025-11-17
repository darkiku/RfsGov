package kz.gov.rfs.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${server.port:8080}")
    private String serverPort;

    @PostMapping("/image")
    @PreAuthorize("hasAnyRole('ADMIN', 'NEWS_MANAGER', 'PROCUREMENT_MANAGER', 'ABOUT_MANAGER', 'SERVICES_MANAGER', 'CONTACTS_MANAGER')")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("📤 Upload image request received. File: {}, Size: {}",
                file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            log.error("❌ File is empty");
            throw new RuntimeException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.error("❌ Invalid content type: {}", contentType);
            throw new RuntimeException("Only image files are allowed");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            log.error("❌ File size exceeds limit: {} bytes", file.getSize());
            throw new RuntimeException("File size exceeds 5MB limit");
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                log.info("📁 Creating upload directory: {}", uploadPath);
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);

            log.info("💾 Saving file to: {}", filePath);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/uploads/" + filename;

            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            response.put("imageUrl", fileUrl);
            response.put("filename", filename);

            log.info("✅ File uploaded successfully: {} -> {}", originalFilename, fileUrl);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("❌ Failed to upload file", e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    @DeleteMapping("/image")
    @PreAuthorize("hasAnyRole('ADMIN', 'NEWS_MANAGER', 'PROCUREMENT_MANAGER', 'ABOUT_MANAGER', 'SERVICES_MANAGER', 'CONTACTS_MANAGER')")
    public ResponseEntity<Map<String, String>> deleteImage(@RequestParam("filename") String filename) {
        log.info("🗑️ Delete image request: {}", filename);

        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("✅ File deleted successfully: {}", filename);

                Map<String, String> response = new HashMap<>();
                response.put("message", "File deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                log.error("❌ File not found: {}", filename);
                throw new RuntimeException("File not found: " + filename);
            }

        } catch (IOException e) {
            log.error("❌ Failed to delete file", e);
            throw new RuntimeException("Failed to delete file: " + e.getMessage());
        }
    }
}