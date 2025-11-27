package kz.gov.rfs.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${server.port:8080}")
    private String serverPort;

    // Разрешенные расширения файлов
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp",
            ".pdf", ".doc", ".docx", ".xls", ".xlsx"
    );

    // Разрешенные MIME типы
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    // Паттерн для проверки UUID-имени файла
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|jpeg|png|gif|webp|pdf|doc|docx|xls|xlsx)$",
            Pattern.CASE_INSENSITIVE
    );

    @PostMapping("/image")
    @PreAuthorize("hasAnyRole('ADMIN', 'NEWS_MANAGER', 'PROCUREMENT_MANAGER', 'ABOUT_MANAGER', 'SERVICES_MANAGER', 'CONTACTS_MANAGER')")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("📤 Upload request: {}, {} bytes", file.getOriginalFilename(), file.getSize());

        try {
            // Валидация файла
            if (file.isEmpty()) {
                log.error("❌ File is empty");
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                log.error("❌ File size exceeds limit: {} bytes", file.getSize());
                return ResponseEntity.badRequest().body(Map.of("error", "File size exceeds 5MB limit"));
            }

            // Проверка MIME типа
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
                log.error("❌ Invalid MIME type: {}", contentType);
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid file type"));
            }

            // Безопасное извлечение расширения
            String extension = extractAndValidateExtension(file.getOriginalFilename());
            if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                log.error("❌ Invalid extension: {}", extension);
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid file extension"));
            }

            // Подготовка безопасного пути
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Генерация безопасного имени файла (UUID)
            String safeFilename = UUID.randomUUID().toString() + extension.toLowerCase();
            Path targetPath = uploadPath.resolve(safeFilename).normalize().toAbsolutePath();

            // Проверка Path Traversal
            if (!targetPath.startsWith(uploadPath)) {
                log.error("🚨 SECURITY: Path traversal detected");
                return ResponseEntity.status(403).body(Map.of("error", "Security violation"));
            }

            // Проверка что файл не существует (защита от перезаписи)
            if (Files.exists(targetPath)) {
                log.error("🚨 SECURITY: File already exists");
                return ResponseEntity.internalServerError().body(Map.of("error", "File generation error"));
            }

            // Сохранение файла БЕЗ REPLACE_EXISTING
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath);
            } catch (FileAlreadyExistsException e) {
                log.error("🚨 File already exists (race condition)");
                return ResponseEntity.internalServerError().body(Map.of("error", "File saving error"));
            }

            // Проверка корректности сохранения
            if (!Files.exists(targetPath) || Files.size(targetPath) != file.getSize()) {
                log.error("❌ File verification failed");
                Files.deleteIfExists(targetPath);
                return ResponseEntity.internalServerError().body(Map.of("error", "Upload verification failed"));
            }

            String fileUrl = "/uploads/" + safeFilename;
            log.info("✅ File uploaded: {}", fileUrl);

            return ResponseEntity.ok(Map.of(
                    "url", fileUrl,
                    "imageUrl", fileUrl,
                    "filename", safeFilename
            ));

        } catch (Exception e) {
            log.error("❌ Upload error", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }

    @DeleteMapping("/image")
    @PreAuthorize("hasAnyRole('ADMIN', 'NEWS_MANAGER', 'PROCUREMENT_MANAGER', 'ABOUT_MANAGER', 'SERVICES_MANAGER', 'CONTACTS_MANAGER')")
    public ResponseEntity<Map<String, String>> deleteImage(@RequestParam("filename") String filename) {
        log.info("🗑️ Delete request: {}", filename);

        try {
            // Валидация filename
            if (filename == null || filename.trim().isEmpty()) {
                log.error("❌ Filename is empty");
                return ResponseEntity.badRequest().body(Map.of("error", "Filename cannot be empty"));
            }

            // Проверка UUID-паттерна (строгая проверка формата)
            if (!UUID_PATTERN.matcher(filename.toLowerCase()).matches()) {
                log.error("🚨 SECURITY: Invalid filename format: {}", filename);
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid filename format"));
            }

            // Безопасное извлечение имени файла
            String safeFilename;
            try {
                safeFilename = Paths.get(filename).getFileName().toString();
            } catch (InvalidPathException e) {
                log.error("🚨 SECURITY: Invalid path: {}", filename);
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid filename"));
            }

            // Проверка что filename не изменился (защита от path traversal)
            if (!filename.equals(safeFilename)) {
                log.error("🚨 SECURITY: Path traversal attempt: {} != {}", filename, safeFilename);
                return ResponseEntity.status(403).body(Map.of("error", "Security violation"));
            }

            // Построение безопасного пути
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path targetPath = uploadPath.resolve(safeFilename).normalize().toAbsolutePath();

            // Проверка Path Traversal
            if (!targetPath.startsWith(uploadPath)) {
                log.error("🚨 SECURITY: Path traversal in delete");
                return ResponseEntity.status(403).body(Map.of("error", "Security violation"));
            }

            // Проверка родительской директории
            Path parentPath = targetPath.getParent();
            if (parentPath == null || !parentPath.equals(uploadPath)) {
                log.error("🚨 SECURITY: Invalid parent directory");
                return ResponseEntity.status(403).body(Map.of("error", "Security violation"));
            }

            // Проверка что это файл, а не директория/симлинк
            if (Files.isDirectory(targetPath) || Files.isSymbolicLink(targetPath)) {
                log.error("🚨 SECURITY: Attempt to delete non-file");
                return ResponseEntity.status(403).body(Map.of("error", "Cannot delete directories"));
            }

            if (!Files.isRegularFile(targetPath)) {
                log.error("🚨 SECURITY: Not a regular file");
                return ResponseEntity.status(403).body(Map.of("error", "Not a regular file"));
            }

            // Удаление файла
            try {
                Files.delete(targetPath);
                log.info("✅ File deleted: {}", safeFilename);
                return ResponseEntity.ok(Map.of("message", "File deleted successfully", "filename", safeFilename));
            } catch (NoSuchFileException e) {
                log.error("❌ File not found: {}", safeFilename);
                return ResponseEntity.status(404).body(Map.of("error", "File not found"));
            } catch (DirectoryNotEmptyException e) {
                log.error("🚨 SECURITY: Attempt to delete directory");
                return ResponseEntity.status(403).body(Map.of("error", "Cannot delete directories"));
            }

        } catch (Exception e) {
            log.error("❌ Delete error", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Безопасное извлечение расширения файла
     */
    private String extractAndValidateExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new SecurityException("Filename is empty");
        }

        // Извлечение только имени файла (без путей)
        String safeFilename;
        try {
            safeFilename = Paths.get(originalFilename).getFileName().toString();
        } catch (InvalidPathException e) {
            throw new SecurityException("Invalid filename path");
        }

        int lastDotIndex = safeFilename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == 0 || lastDotIndex == safeFilename.length() - 1) {
            throw new SecurityException("File has no valid extension");
        }

        String extension = safeFilename.substring(lastDotIndex);
        if (!extension.matches("^\\.[a-zA-Z0-9]+$")) {
            throw new SecurityException("Invalid extension format");
        }

        return extension;
    }
}