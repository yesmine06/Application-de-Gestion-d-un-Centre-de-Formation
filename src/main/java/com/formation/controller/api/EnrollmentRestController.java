package com.formation.controller.api;

import com.formation.dto.EnrollmentDto;
import com.formation.dto.EnrollmentRequestDto;
import com.formation.entity.Enrollment;
import com.formation.exception.ResourceNotFoundException;
import com.formation.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inscriptions")
public class EnrollmentRestController extends BaseRestController<Enrollment, Long, EnrollmentDto> {
    
    private final EnrollmentService enrollmentService;
    
    public EnrollmentRestController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }
    
    @Override
    protected Enrollment findByIdOrThrow(Long id) {
        return enrollmentService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inscription non trouvée avec l'ID: " + id));
    }
    
    @Override
    protected List<Enrollment> findAll() {
        return enrollmentService.findAll();
    }
    
    @Override
    protected Page<Enrollment> findAll(Pageable pageable) {
        // EnrollmentService n'a pas de pagination, retourner une page vide
        return Page.empty();
    }
    
    @Override
    protected Enrollment save(Enrollment entity) {
        return enrollmentService.save(entity);
    }
    
    @Override
    protected Enrollment update(Long id, Enrollment entity) {
        return enrollmentService.save(entity);
    }
    
    @Override
    protected void delete(Long id) {
        enrollmentService.delete(id);
    }
    
    @Override
    protected EnrollmentDto toDto(Enrollment entity) {
        return EnrollmentDto.fromEntity(entity);
    }
    
    @Override
    protected String getEntityName() {
        return "Inscription";
    }
    
    /**
     * POST /create - Créer une inscription
     */
    @PostMapping("/create")
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


