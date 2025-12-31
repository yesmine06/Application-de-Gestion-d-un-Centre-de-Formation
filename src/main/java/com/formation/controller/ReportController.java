package com.formation.controller;

import com.formation.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reports")
public class ReportController {
    
    private final ReportService reportService;
    
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }
    
    /**
     * Génère un rapport PDF des notes pour un étudiant
     */
    @GetMapping("/student/{studentId}/grades")
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATEUR', 'ETUDIANT')")
    public ResponseEntity<byte[]> generateStudentGradesReport(@PathVariable Long studentId) {
        try {
            // Pour l'instant, retourner un message d'information
            // La génération PDF complète nécessite un template JasperReports
            String message = "Génération de rapport PDF pour l'étudiant ID: " + studentId + 
                           "\n\nPour activer cette fonctionnalité, veuillez créer un template JasperReports (.jrxml) " +
                           "dans src/main/resources/reports/student-grades.jrxml";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "student-grades-info.txt");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(message.getBytes());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Génère un rapport PDF des notes pour un cours
     */
    @GetMapping("/course/{courseId}/grades")
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATEUR')")
    public ResponseEntity<byte[]> generateCourseGradesReport(@PathVariable Long courseId) {
        try {
            String message = "Génération de rapport PDF pour le cours ID: " + courseId + 
                           "\n\nPour activer cette fonctionnalité, veuillez créer un template JasperReports (.jrxml) " +
                           "dans src/main/resources/reports/course-grades.jrxml";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "course-grades-info.txt");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(message.getBytes());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

