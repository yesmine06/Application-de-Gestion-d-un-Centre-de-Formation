package com.formation.controller.api;

import com.formation.entity.Schedule;
import com.formation.exception.ResourceNotFoundException;
import com.formation.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleRestController {
    
    private final ScheduleService scheduleService;
    
    public ScheduleRestController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }
    
    /**
     * GET / - Liste de toutes les séances (optionnellement filtrée par date)
     */
    @GetMapping
    public ResponseEntity<List<Schedule>> getAllSchedules(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Schedule> schedules = scheduleService.findAll();
        if (date != null) {
            schedules = schedules.stream()
                .filter(s -> s.getDate().equals(date))
                .toList();
        }
        return ResponseEntity.ok(schedules);
    }
    
    /**
     * GET /{id} - Obtenir une séance par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Schedule> getScheduleById(@PathVariable Long id) {
        Schedule schedule = scheduleService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));
        return ResponseEntity.ok(schedule);
    }
    
    /**
     * GET /student/{studentId} - Séances d'un étudiant
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Schedule>> getStudentSchedules(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate scheduleDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(scheduleService.getStudentSchedule(studentId, scheduleDate));
    }
    
    /**
     * GET /trainer/{trainerId} - Séances d'un formateur
     */
    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<List<Schedule>> getTrainerSchedules(@PathVariable Long trainerId) {
        List<Schedule> schedules = scheduleService.findByTrainer(trainerId);
        return ResponseEntity.ok(schedules);
    }
    
    /**
     * GET /course/{courseId} - Séances d'un cours
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Schedule>> getCourseSchedules(@PathVariable Long courseId) {
        List<Schedule> schedules = scheduleService.findByCourse(courseId);
        return ResponseEntity.ok(schedules);
    }
    
    /**
     * POST / - Créer une nouvelle séance
     * Seuls les formateurs et admins peuvent créer des séances
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN')")
    public ResponseEntity<Schedule> createSchedule(@Valid @RequestBody Schedule schedule) {
        Schedule savedSchedule = scheduleService.save(schedule);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSchedule);
    }
    
    /**
     * PUT /{id} - Mettre à jour une séance
     * Seuls les formateurs et admins peuvent modifier
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN')")
    public ResponseEntity<Schedule> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody Schedule scheduleDetails) {
        Schedule updatedSchedule = scheduleService.update(id, scheduleDetails);
        return ResponseEntity.ok(updatedSchedule);
    }
    
    /**
     * DELETE /{id} - Supprimer une séance
     * Seuls les formateurs et admins peuvent supprimer
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN')")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * POST /{id}/approve - Approuver une séance (admin uniquement)
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Schedule> approveSchedule(@PathVariable Long id) {
        Schedule approvedSchedule = scheduleService.approve(id);
        return ResponseEntity.ok(approvedSchedule);
    }
    
    /**
     * POST /{id}/reject - Rejeter une séance (admin uniquement)
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Schedule> rejectSchedule(@PathVariable Long id) {
        Schedule rejectedSchedule = scheduleService.reject(id);
        return ResponseEntity.ok(rejectedSchedule);
    }
}

