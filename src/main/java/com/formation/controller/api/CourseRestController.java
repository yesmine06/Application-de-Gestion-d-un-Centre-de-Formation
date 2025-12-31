package com.formation.controller.api;

import com.formation.dto.CourseDto;
import com.formation.entity.Course;
import com.formation.exception.ResourceNotFoundException;
import com.formation.service.CourseDtoMapper;
import com.formation.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cours")
public class CourseRestController extends BaseRestController<Course, Long, CourseDto> {
    
    private final CourseService courseService;
    private final CourseDtoMapper courseDtoMapper;
    
    public CourseRestController(CourseService courseService, CourseDtoMapper courseDtoMapper) {
        this.courseService = courseService;
        this.courseDtoMapper = courseDtoMapper;
    }
    
    @Override
    protected Course findByIdOrThrow(Long id) {
        return courseService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé avec l'ID: " + id));
    }
    
    @Override
    protected List<Course> findAll() {
        return courseService.findAll();
    }
    
    @Override
    protected Page<Course> findAll(Pageable pageable) {
        return courseService.findAll(pageable);
    }
    
    @Override
    protected Course save(Course entity) {
        return courseService.save(entity);
    }
    
    @Override
    protected Course update(Long id, Course entity) {
        return courseService.update(id, entity);
    }
    
    @Override
    protected void delete(Long id) {
        courseService.delete(id);
    }
    
    @Override
    protected CourseDto toDto(Course entity) {
        return courseDtoMapper.toDto(entity);
    }
    
    @Override
    protected String getEntityName() {
        return "Cours";
    }
    
    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<List<Course>> getCoursesByTrainer(@PathVariable Long trainerId) {
        return ResponseEntity.ok(courseService.findByTrainer(trainerId));
    }
}


