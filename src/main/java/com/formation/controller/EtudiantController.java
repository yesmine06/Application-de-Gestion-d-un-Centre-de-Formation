package com.formation.controller;

import com.formation.entity.*;
import com.formation.service.EtudiantService;
import com.formation.service.GradeService;
import com.formation.service.ScheduleService;
import com.formation.service.EnrollmentService;
import com.formation.service.CourseFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Contrôleur Étudiant - DÉSACTIVÉ (remplacé par interface React)
 * Tous les endpoints redirigent vers l'interface React
 */
@Controller
@RequestMapping("/etudiant")
@PreAuthorize("hasAnyRole('ETUDIANT', 'ADMIN', 'FORMATEUR')")
public class EtudiantController extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(EtudiantController.class);
    
    private final EtudiantService etudiantService;
    private final GradeService gradeService;
    private final ScheduleService scheduleService;
    private final EnrollmentService enrollmentService;
    private final CourseFileService courseFileService;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public EtudiantController(EtudiantService etudiantService,
                             GradeService gradeService,
                             ScheduleService scheduleService,
                             EnrollmentService enrollmentService,
                             CourseFileService courseFileService,
                             ObjectMapper objectMapper) {
        this.etudiantService = etudiantService;
        this.gradeService = gradeService;
        this.scheduleService = scheduleService;
        this.enrollmentService = enrollmentService;
        this.courseFileService = courseFileService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Redirige vers l'interface React
     */
    @GetMapping("/courses/available")
    public String availableCourses() {
        return "redirect:/react/etudiant/index.html";
    }
    
    @PostMapping("/courses/{courseId}/enroll")
    public String enrollInCourse(@PathVariable Long courseId) {
        return "redirect:/react/etudiant/index.html";
    }
    
    @PostMapping("/courses/{courseId}/unenroll")
    public String unenrollFromCourse(@PathVariable Long courseId) {
        return "redirect:/react/etudiant/index.html";
    }
    
    @GetMapping("/courses")
    public String myCourses() {
        return "redirect:/react/etudiant/index.html";
    }
    
    @GetMapping("/courses/{courseId}")
    public String courseDetails(@PathVariable Long courseId) {
        return "redirect:/react/etudiant/index.html";
    }
    
    @GetMapping("/grades")
    public String myGrades() {
        return "redirect:/react/etudiant/index.html";
    }
    
    @GetMapping("/schedule")
    public String mySchedule() {
        return "redirect:/react/etudiant/index.html";
    }
    
    /**
     * Télécharge un fichier d'un cours (conservé pour l'API)
     */
    @GetMapping("/courses/{courseId}/files/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long courseId,
                                                 @PathVariable Long fileId,
                                                 Authentication authentication) {
        try {
            Student student = etudiantService.getCurrentStudent(getUsername(authentication));
            
            // Vérifier que l'étudiant est inscrit au cours
            if (!etudiantService.isEnrolledInCourse(student.getId(), courseId)) {
                return ResponseEntity.notFound().build();
            }
            
            CourseFile file = courseFileService.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Fichier non trouvé"));
            
            Path filePath = courseFileService.getFilePath(fileId);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.getContentType() != null ? file.getContentType() : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getOriginalFileName() + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

