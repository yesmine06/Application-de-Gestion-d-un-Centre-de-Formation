package com.formation.controller.api;

import com.formation.dto.EnrollmentDto;
import com.formation.dto.EnrollmentRequestDto;
import com.formation.entity.Enrollment;
import com.formation.exception.ResourceNotFoundException;
import com.formation.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inscriptions")
public class EnrollmentRestController {
    
    private final EnrollmentService enrollmentService;
    
    public EnrollmentRestController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }
    
    private EnrollmentDto toDto(Enrollment entity) {
        return EnrollmentDto.fromEntity(entity);
    }
    
    /**
     * GET / - Liste des inscriptions
     */
    @GetMapping
    public ResponseEntity<List<EnrollmentDto>> getAll() {
        List<Enrollment> enrollments = enrollmentService.findAll();
        List<EnrollmentDto> dtos = enrollments.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * GET /{id} - Trouver par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentDto> getById(@PathVariable Long id) {
        Enrollment enrollment = enrollmentService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inscription non trouvée avec l'ID: " + id));
        return ResponseEntity.ok(toDto(enrollment));
    }
    
    /**
     * POST / - Créer une inscription
     * Accepte EnrollmentRequestDto au lieu d'Enrollment
     */
    @PostMapping
    public ResponseEntity<EnrollmentDto> create(@Valid @RequestBody EnrollmentRequestDto request) {
        // Validation et logging
        if (request.getStudentId() == null) {
            throw new IllegalArgumentException("L'ID de l'étudiant est requis");
        }
        if (request.getCoursId() == null) {
            throw new IllegalArgumentException("L'ID du cours est requis");
        }
        
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EnrollmentRestController.class);
        logger.info("Création d'inscription - StudentId: {}, CoursId: {}", request.getStudentId(), request.getCoursId());
        
        Enrollment enrollment = enrollmentService.enroll(request.getStudentId(), request.getCoursId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(enrollment));
    }
    
    /**
     * POST /create - Créer une inscription (endpoint alternatif pour compatibilité)
     */
    @PostMapping(value = "/create", consumes = "application/json")
    public ResponseEntity<EnrollmentDto> createEnrollment(@Valid @RequestBody EnrollmentRequestDto request) {
        Enrollment enrollment = enrollmentService.enroll(request.getStudentId(), request.getCoursId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(enrollment));
    }
    
    /**
     * DELETE /student/{studentId}/course/{coursId} - Désinscrire un étudiant d'un cours
     */
    @DeleteMapping("/student/{studentId}/course/{coursId}")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long studentId, 
                                                 @PathVariable Long coursId) {
        enrollmentService.unenroll(studentId, coursId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * GET /student/{studentId} - Obtenir les inscriptions d'un étudiant
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EnrollmentDto>> getEnrollmentsByStudent(@PathVariable Long studentId) {
        List<Enrollment> enrollments = enrollmentService.findByStudent(studentId);
        List<EnrollmentDto> dtos = enrollments.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * GET /course/{coursId} - Obtenir les inscriptions d'un cours
     */
    @GetMapping("/course/{coursId}")
    public ResponseEntity<List<EnrollmentDto>> getEnrollmentsByCourse(@PathVariable Long coursId) {
        List<Enrollment> enrollments = enrollmentService.findByCourse(coursId);
        List<EnrollmentDto> dtos = enrollments.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}


