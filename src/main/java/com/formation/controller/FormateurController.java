package com.formation.controller;

import com.formation.entity.*;
import com.formation.exception.BusinessException;
import com.formation.exception.ResourceNotFoundException;
import com.formation.service.FormateurService;
import com.formation.service.GradeService;
import com.formation.service.CourseFileService;
import com.formation.service.CourseService;
import com.formation.service.SessionService;
import com.formation.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Contrôleur Formateur - DÉSACTIVÉ (remplacé par interface React)
 * Tous les endpoints redirigent vers l'interface React
 * Les endpoints de téléchargement de fichiers sont conservés pour l'API
 */
@Controller
@RequestMapping("/formateur")
@PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN')")
public class FormateurController extends BaseController {
    
    private final FormateurService formateurService;
    private final GradeService gradeService;
    private final CourseFileService courseFileService;
    private final CourseService courseService;
    private final SessionService sessionService;
    private final ScheduleService scheduleService;
    
    @Autowired
    public FormateurController(FormateurService formateurService,
                              GradeService gradeService,
                              CourseFileService courseFileService,
                              CourseService courseService,
                              SessionService sessionService,
                              ScheduleService scheduleService) {
        this.formateurService = formateurService;
        this.gradeService = gradeService;
        this.courseFileService = courseFileService;
        this.courseService = courseService;
        this.sessionService = sessionService;
        this.scheduleService = scheduleService;
    }
    
    /**
     * Redirige vers l'interface React
     */
    @GetMapping("/courses")
    public String myCourses() {
        return "redirect:/react/formateur/index.html";
    }
    
    @GetMapping("/courses/{courseId}")
    public String courseDetails(@PathVariable Long courseId) {
        return "redirect:/react/formateur/index.html";
    }
    
    @GetMapping("/students")
    public String myStudents() {
        return "redirect:/react/formateur/index.html";
    }
    
    @GetMapping("/grades")
    public String myGrades() {
        return "redirect:/react/formateur/index.html";
    }
    
    @GetMapping("/grades/new")
    public String showGradeForm() {
        return "redirect:/react/formateur/index.html";
    }
    
    @PostMapping("/grades/save")
    public String saveGrade(@RequestParam Long courseId,
                           @RequestParam Long studentId,
                           @RequestParam Double valeur,
                           @RequestParam(required = false) String commentaire,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            
            // Vérifier que le cours appartient au formateur
            Course course = formateurService.getTrainerCourse(trainer.getId(), courseId)
                .orElseThrow(() -> new BusinessException("Cours non trouvé ou ne vous appartient pas"));
            
            // Utiliser le GradeService pour sauvegarder
            gradeService.saveOrUpdate(studentId, courseId, valeur, commentaire);
            
            redirectAttributes.addFlashAttribute("success", "Note attribuée avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/react/formateur/index.html";
    }
    
    @GetMapping("/courses/{courseId}/files")
    public String courseFiles(@PathVariable Long courseId) {
        return "redirect:/react/formateur/index.html";
    }
    
    @PostMapping("/courses/{courseId}/files/upload")
    public String uploadFile(@PathVariable Long courseId,
                            @RequestParam("file") MultipartFile file,
                            @RequestParam(required = false) String description,
                            RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("info", "Utilisez l'interface React pour uploader des fichiers");
        return "redirect:/react/formateur/index.html";
    }
    
    /**
     * Télécharge un fichier (conservé pour l'API)
     */
    @GetMapping("/courses/{courseId}/files/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long courseId,
                                                 @PathVariable Long fileId,
                                                 Authentication authentication) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            Course course = formateurService.getTrainerCourse(trainer.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé"));
            
            CourseFile file = courseFileService.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Fichier non trouvé"));
            
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
    
    @PostMapping("/courses/{courseId}/files/{fileId}/delete")
    public String deleteFile(@PathVariable Long courseId,
                            @PathVariable Long fileId) {
        return "redirect:/react/formateur/index.html";
    }
    
    @GetMapping("/courses/new")
    public String showCreateCourseForm() {
        return "redirect:/react/formateur/index.html";
    }
    
    @PostMapping("/courses")
    public String createCourse() {
        return "redirect:/react/formateur/index.html";
    }
    
    @GetMapping("/groups")
    public String viewGroups() {
        return "redirect:/react/formateur/index.html";
    }
    
    @GetMapping("/schedules")
    public String mySchedules() {
        return "redirect:/react/formateur/index.html";
    }
    
    @GetMapping("/courses/{courseId}/schedules/new")
    public String showCreateScheduleForm(@PathVariable Long courseId) {
        return "redirect:/react/formateur/index.html";
    }
    
    @PostMapping("/courses/{courseId}/schedules")
    public String createSchedule(@PathVariable Long courseId) {
        return "redirect:/react/formateur/index.html";
    }
    
    @GetMapping("/schedules/{scheduleId}/edit")
    public String showEditScheduleForm(@PathVariable Long scheduleId) {
        return "redirect:/react/formateur/index.html";
    }
    
    @PostMapping("/schedules/{scheduleId}")
    public String updateSchedule(@PathVariable Long scheduleId) {
        return "redirect:/react/formateur/index.html";
    }
    
    @PostMapping("/schedules/{scheduleId}/delete")
    public String deleteSchedule(@PathVariable Long scheduleId) {
        return "redirect:/react/formateur/index.html";
    }
}

