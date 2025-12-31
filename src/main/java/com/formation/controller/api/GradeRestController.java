package com.formation.controller.api;

import com.formation.dto.GradeDto;
import com.formation.dto.GradeRequestDto;
import com.formation.entity.Grade;
import com.formation.exception.ResourceNotFoundException;
import com.formation.service.GradeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grades")
public class GradeRestController extends BaseRestController<Grade, Long, GradeDto> {
    
    private final GradeService gradeService;
    
    public GradeRestController(GradeService gradeService) {
        this.gradeService = gradeService;
    }
    
    @Override
    protected Grade findByIdOrThrow(Long id) {
        return gradeService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Note non trouvée avec l'ID: " + id));
    }
    
    @Override
    protected List<Grade> findAll() {
        return gradeService.findAll();
    }
    
    @Override
    protected Page<Grade> findAll(Pageable pageable) {
        // GradeService n'a pas de pagination, retourner une page vide
        return Page.empty();
    }
    
    @Override
    protected Grade save(Grade entity) {
        return gradeService.save(entity);
    }
    
    @Override
    protected Grade update(Long id, Grade entity) {
        return gradeService.save(entity);
    }
    
    @Override
    protected void delete(Long id) {
        gradeService.deleteById(id);
    }
    
    @Override
    protected GradeDto toDto(Grade entity) {
        return GradeDto.fromEntity(entity);
    }
    
    @Override
    protected String getEntityName() {
        return "Note";
    }
    
    /**
     * POST /create - Créer ou mettre à jour une note
     */
    @PostMapping("/create")
    public ResponseEntity<GradeDto> createOrUpdateGrade(@Valid @RequestBody GradeRequestDto request) {
        Grade grade = gradeService.saveOrUpdate(
            request.getStudentId(), 
            request.getCoursId(), 
            request.getValeur(), 
            request.getCommentaire()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(grade));
    }
    
    /**
     * GET /student/{studentId} - Obtenir les notes d'un étudiant
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<GradeDto>> getGradesByStudent(@PathVariable Long studentId) {
        List<Grade> grades = gradeService.findByStudent(studentId);
        List<GradeDto> dtos = grades.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * GET /course/{coursId} - Obtenir les notes d'un cours
     */
    @GetMapping("/course/{coursId}")
    public ResponseEntity<List<GradeDto>> getGradesByCourse(@PathVariable Long coursId) {
        List<Grade> grades = gradeService.findByCourse(coursId);
        List<GradeDto> dtos = grades.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * GET /student/{studentId}/average - Calculer la moyenne d'un étudiant
     */
    @GetMapping("/student/{studentId}/average")
    public ResponseEntity<Map<String, Double>> getStudentAverage(@PathVariable Long studentId) {
        Double average = gradeService.calculateStudentAverage(studentId);
        return ResponseEntity.ok(Map.of("average", average != null ? average : 0.0));
    }
    
    /**
     * GET /course/{coursId}/success-rate - Calculer le taux de réussite d'un cours
     */
    @GetMapping("/course/{coursId}/success-rate")
    public ResponseEntity<Map<String, Double>> getCourseSuccessRate(@PathVariable Long coursId) {
        Double rate = gradeService.calculateCourseSuccessRate(coursId);
        return ResponseEntity.ok(Map.of("successRate", rate != null ? rate : 0.0));
    }
}

