package com.example.carnet_de_sante.infrastructure.driving.web;

import com.example.carnet_de_sante.domain.model.Patient;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileUploadController {

    private final PatientFileRepository fileRepository;
    private final PatientRepository patientRepository;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final String UPLOAD_DIR = "uploads/files/";

    private Patient getAuthenticatedPatient(UserDetails userDetails) {
        return patientRepository.findByUser_Email(userDetails.getUsername()).orElse(null);
    }

    private static String sanitizeFilename(String name) {
        if (name == null) return "fichier";
        // Retire tout ce qui n'est pas alphanumérique, point, tiret ou underscore
        return name.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
    }

    @PostMapping(value = "/thumbnail-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestParam("thumbnail") MultipartFile file,
            @RequestParam(required = false) Long patientId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Patient authenticatedPatient = getAuthenticatedPatient(userDetails);
        if (authenticatedPatient == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Vérifie que l'upload est bien pour le patient authentifié
        if (patientId == null || !authenticatedPatient.getId().equals(patientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.contains("pdf")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only PDF files are allowed"));
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body(Map.of("error", "File size exceeds 10MB"));
            }

            Path uploadPath = Paths.get(UPLOAD_DIR);
            Files.createDirectories(uploadPath);

            String originalFilename = sanitizeFilename(file.getOriginalFilename());
            String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
            Path filePath = uploadPath.resolve(storedFilename);

            // Vérifie que le chemin résolu est bien dans le dossier uploads (anti path traversal)
            if (!filePath.normalize().startsWith(uploadPath.toAbsolutePath())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid file name"));
            }

            Files.write(filePath, file.getBytes());

            PatientFile patientFile = new PatientFile();
            patientFile.setPatient(authenticatedPatient);
            patientFile.setFileName(originalFilename);
            patientFile.setFilePath(filePath.toString());
            patientFile.setFileType(contentType);
            patientFile.setFileSize(file.getSize());
            patientFile.setUploadedAt(LocalDateTime.now());

            PatientFile saved = fileRepository.save(patientFile);

            return ResponseEntity.ok(Map.of(
                    "id", saved.getId(),
                    "fileName", saved.getFileName(),
                    "fileType", saved.getFileType(),
                    "fileSize", saved.getFileSize(),
                    "uploadedAt", saved.getUploadedAt(),
                    "message", "File uploaded successfully"
            ));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload failed"));
        }
    }

    @GetMapping("/files/{patientId}")
    public ResponseEntity<?> getFiles(
            @PathVariable Long patientId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Patient authenticatedPatient = getAuthenticatedPatient(userDetails);
        if (authenticatedPatient == null || !authenticatedPatient.getId().equals(patientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<PatientFile> files = fileRepository.findByPatientIdAndDeletedAtIsNull(patientId);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/files/download/{id}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Patient authenticatedPatient = getAuthenticatedPatient(userDetails);
        if (authenticatedPatient == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PatientFile file = fileRepository.findById(id).orElse(null);
        if (file == null || !file.getPatient().getId().equals(authenticatedPatient.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

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
    }

    @DeleteMapping("/files/{id}")
    public ResponseEntity<?> deleteFile(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Patient authenticatedPatient = getAuthenticatedPatient(userDetails);
        if (authenticatedPatient == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PatientFile file = fileRepository.findById(id).orElse(null);
        if (file == null || !file.getPatient().getId().equals(authenticatedPatient.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        file.setDeletedAt(LocalDateTime.now());
        fileRepository.save(file);

        try {
            Path filePath = Paths.get(file.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            System.err.println("Failed to delete file from disk: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
    }
}
