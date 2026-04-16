package com.example.carnet_de_sante.infrastructure.driving.web;

import com.example.carnet_de_sante.domain.model.PatientFile;
import com.example.carnet_de_sante.domain.port.PatientFileRepository;
import com.example.carnet_de_sante.domain.port.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileUploadController {

    private final PatientFileRepository fileRepository;
    private final PatientRepository patientRepository;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String UPLOAD_DIR = "uploads/files/";

    @PostMapping(value = "/thumbnail-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestParam("thumbnail") MultipartFile file,
            @RequestParam(required = false) Long patientId) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }

            // Validate file type (PDF only)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.contains("pdf")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only PDF files are allowed"));
            }

            // Validate file size
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body(Map.of("error", "File size exceeds 10MB"));
            }

            // Create upload directory if needed
            Path uploadPath = Paths.get(UPLOAD_DIR);
            Files.createDirectories(uploadPath);

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileName = UUID.randomUUID().toString() + "_" + originalFilename;
            Path filePath = uploadPath.resolve(fileName);

            // Save file to disk
            Files.write(filePath, file.getBytes());

            // Save to database if patientId provided
            if (patientId != null) {
                return patientRepository.findById(patientId)
                        .map(patient -> {
                            PatientFile patientFile = new PatientFile();
                            patientFile.setPatient(patient);
                            patientFile.setFileName(originalFilename);
                            patientFile.setFilePath(filePath.toString());
                            patientFile.setFileType(contentType);
                            patientFile.setFileSize(file.getSize());
                            patientFile.setUploadedAt(LocalDateTime.now());
                            
                            PatientFile saved = fileRepository.save(patientFile);
                            
                            Map<String, Object> response = new HashMap<>();
                            response.put("id", saved.getId());
                            response.put("fileName", saved.getFileName());
                            response.put("fileType", saved.getFileType());
                            response.put("fileSize", saved.getFileSize());
                            response.put("uploadedAt", saved.getUploadedAt());
                            response.put("message", "File uploaded successfully");
                            
                            return ResponseEntity.ok(response);
                        })
                        .orElse(ResponseEntity.notFound().build());
            }

            // Generic response if no patientId
            Map<String, Object> response = new HashMap<>();
            response.put("fileName", originalFilename);
            response.put("fileType", contentType);
            response.put("fileSize", file.getSize());
            response.put("uploadedAt", LocalDateTime.now());
            response.put("message", "File uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/files/{patientId}")
    public ResponseEntity<?> getFiles(@PathVariable Long patientId) {
        try {
            List<PatientFile> files = fileRepository
                    .findByPatientIdAndDeletedAtIsNull(patientId);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to retrieve files: " + e.getMessage()));
        }
    }

    @GetMapping("/files/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            PatientFile file = fileRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            Path filePath = Paths.get(file.getFilePath());
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + file.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/files/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable Long id) {
        try {
            PatientFile file = fileRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            // Soft delete - mark as deleted
            file.setDeletedAt(LocalDateTime.now());
            fileRepository.save(file);

            // Optionally delete file from disk
            try {
                Path filePath = Paths.get(file.getFilePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            } catch (IOException e) {
                // Log but don't fail if file deletion fails
                System.err.println("Failed to delete file: " + e.getMessage());
            }

            return ResponseEntity.ok(Map.of("message", "File deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "File not found"));
        }
    }
}
