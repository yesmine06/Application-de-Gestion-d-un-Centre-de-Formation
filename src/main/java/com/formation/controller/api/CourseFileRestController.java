package com.formation.controller.api;

import com.formation.entity.CourseFile;
import com.formation.exception.ResourceNotFoundException;
import com.formation.service.CourseFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/course-files")
@PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN', 'ETUDIANT')")
public class CourseFileRestController {
    
    private final CourseFileService courseFileService;
    
    @Autowired
    public CourseFileRestController(CourseFileService courseFileService) {
        this.courseFileService = courseFileService;
    }
    
    /**
     * GET /course/{courseId} - Liste des fichiers d'un cours
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseFile>> getFilesByCourse(@PathVariable Long courseId) {
        List<CourseFile> files = courseFileService.getFilesByCourse(courseId);
        return ResponseEntity.ok(files);
    }
    
    /**
     * POST /upload - Upload un fichier pour un cours
     * Seuls les formateurs et admins peuvent uploader
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN')")
    public ResponseEntity<CourseFile> uploadFile(
            @RequestParam("courseId") Long courseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description,
            Authentication authentication) {
        try {
            CourseFile uploadedFile = courseFileService.uploadFile(courseId, file, description);
            return ResponseEntity.ok(uploadedFile);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /{fileId}/download - Télécharger un fichier
     */
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        try {
            CourseFile file = courseFileService.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Fichier non trouvé"));
            
            Path filePath = courseFileService.getFilePath(fileId);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(
                        file.getContentType() != null ? file.getContentType() : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + file.getOriginalFileName() + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * DELETE /{fileId} - Supprimer un fichier
     * Seuls les formateurs et admins peuvent supprimer
     */
    @DeleteMapping("/{fileId}")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN')")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) {
        try {
            courseFileService.deleteFile(fileId);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /{fileId} - Obtenir les détails d'un fichier
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<CourseFile> getFile(@PathVariable Long fileId) {
        CourseFile file = courseFileService.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("Fichier non trouvé"));
        return ResponseEntity.ok(file);
    }
}

