package com.formation.controller.api;

import com.formation.entity.Schedule;
import com.formation.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
    
    @GetMapping
    public ResponseEntity<List<Schedule>> getAllSchedules(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Schedule> schedules = scheduleService.findAll();
        if (date != null) {
            // Filter by date if provided
            schedules = schedules.stream()
                .filter(s -> s.getDate().equals(date))
                .toList();
        }
        return ResponseEntity.ok(schedules);
    }
    
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Schedule>> getStudentSchedules(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate scheduleDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(scheduleService.getStudentSchedule(studentId, scheduleDate));
    }
}

