package com.formation.service;

import com.formation.constants.AppConstants;
import com.formation.entity.Course;
import com.formation.entity.CourseFile;
import com.formation.exception.BusinessException;
import com.formation.exception.ResourceNotFoundException;
import com.formation.repository.CourseFileRepository;
import com.formation.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CourseFileService {
    
    private static final String[] ALLOWED_EXTENSIONS = {".pdf", ".doc", ".docx", ".txt", ".xls", ".xlsx", ".ppt", ".pptx"};
    
    private final CourseFileRepository courseFileRepository;
    private final CourseRepository courseRepository;
    
    @Autowired
    public CourseFileService(CourseFileRepository courseFileRepository, CourseRepository courseRepository) {
        this.courseFileRepository = courseFileRepository;
        this.courseRepository = courseRepository;
    }
    
    /**
     * Upload un fichier pour un cours
     */
    public CourseFile uploadFile(Long courseId, MultipartFile file, String description) throws IOException {
        validateFile(file);
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé"));
        
        // Créer le répertoire s'il n'existe pas
        Path uploadPath = Paths.get(AppConstants.UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Générer un nom de fichier unique
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new BusinessException("Le nom du fichier est requis");
        }
        
        String fileExtension = originalFileName.contains(".") 
            ? originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase()
            : "";
        
        // Vérifier l'extension
        if (!isAllowedExtension(fileExtension)) {
            throw new BusinessException("Type de fichier non autorisé. Extensions autorisées: " + 
                String.join(", ", ALLOWED_EXTENSIONS));
        }
        
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(uniqueFileName);
        
        // Sauvegarder le fichier
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Créer l'entité CourseFile
        CourseFile courseFile = new CourseFile();
        courseFile.setFileName(uniqueFileName);
        courseFile.setOriginalFileName(originalFileName);
        courseFile.setFilePath(filePath.toString());
        courseFile.setFileSize(file.getSize());
        courseFile.setContentType(file.getContentType());
        courseFile.setDescription(description);
        courseFile.setCourse(course);
        
        return courseFileRepository.save(courseFile);
    }
    
    /**
     * Valide le fichier uploadé
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Le fichier est vide");
        }
        
        if (file.getSize() > AppConstants.MAX_FILE_SIZE_BYTES) {
            throw new BusinessException("Le fichier est trop volumineux. Taille maximale: " + 
                (AppConstants.MAX_FILE_SIZE_BYTES / 1024 / 1024) + "MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new BusinessException("Type de contenu non autorisé. Seuls les fichiers PDF, Word, Excel et PowerPoint sont autorisés.");
        }
    }
    
    /**
     * Vérifie si l'extension est autorisée
     */
    private boolean isAllowedExtension(String extension) {
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Vérifie si le type de contenu est autorisé
     */
    private boolean isAllowedContentType(String contentType) {
        String[] allowedTypes = {
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        };
        
        for (String allowed : allowedTypes) {
            if (contentType.startsWith(allowed)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Récupère tous les fichiers d'un cours
     */
    public List<CourseFile> getFilesByCourse(Long courseId) {
        return courseFileRepository.findByCourseId(courseId);
    }
    
    /**
     * Supprime un fichier
     */
    public void deleteFile(Long fileId) throws IOException {
        CourseFile file = courseFileRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("Fichier non trouvé"));
        
        // Supprimer le fichier physique
        Path filePath = Paths.get(file.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        
        // Supprimer l'enregistrement en base
        courseFileRepository.delete(file);
    }
    
    /**
     * Récupère un fichier par son ID
     */
    public Optional<CourseFile> findById(Long fileId) {
        return courseFileRepository.findById(fileId);
    }
    
    /**
     * Récupère le chemin du fichier pour le téléchargement
     */
    public Path getFilePath(Long fileId) {
        CourseFile file = courseFileRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("Fichier non trouvé"));
        return Paths.get(file.getFilePath());
    }
}

