package com.formation.controller.api;

import com.formation.dto.CourseCreateDto;
import com.formation.dto.CourseDto;
import com.formation.entity.Course;
import com.formation.entity.Session;
import com.formation.entity.Trainer;
import com.formation.entity.User;
import com.formation.exception.ResourceNotFoundException;
import com.formation.repository.SessionRepository;
import com.formation.repository.UserRepository;
import com.formation.service.CourseDtoMapper;
import com.formation.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cours")
public class CourseRestController extends BaseRestController<Course, Long, CourseDto> {
    
    private final CourseService courseService;
    private final CourseDtoMapper courseDtoMapper;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    
    public CourseRestController(CourseService courseService, CourseDtoMapper courseDtoMapper, 
                                UserRepository userRepository, SessionRepository sessionRepository) {
        this.courseService = courseService;
        this.courseDtoMapper = courseDtoMapper;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
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
    public ResponseEntity<List<CourseDto>> getCoursesByTrainer(@PathVariable Long trainerId) {
        List<Course> courses = courseService.findByTrainer(trainerId);
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CourseRestController.class);
        logger.info("Récupération des cours pour le formateur ID: {}, nombre de cours: {}", trainerId, courses.size());
        List<CourseDto> dtos = courses.stream()
            .map(courseDtoMapper::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * POST /trainer/create - Créer un cours par un formateur
     * Le formateur connecté est automatiquement assigné comme formateur du cours
     */
    @PostMapping("/trainer/create")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<CourseDto> createCourseByTrainer(
            @Valid @RequestBody CourseCreateDto courseDto,
            Authentication authentication) {
        
        // Récupérer le formateur connecté
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        
        if (!(user instanceof Trainer)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Trainer trainer = (Trainer) user;
        
        // Créer l'entité Course à partir du DTO
        Course course = new Course();
        course.setCode(courseDto.getCode());
        course.setTitre(courseDto.getTitre());
        course.setDescription(courseDto.getDescription());
        course.setFormateur(trainer); // Assigner le formateur connecté
        
        // Assigner la session si fournie
        if (courseDto.getSessionId() != null) {
            Session session = sessionRepository.findById(courseDto.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session non trouvée avec l'ID: " + courseDto.getSessionId()));
            course.setSession(session);
        }
        
        // Sauvegarder le cours
        Course savedCourse = courseService.save(course);
        
        // Retourner le DTO
        return ResponseEntity.status(HttpStatus.CREATED).body(courseDtoMapper.toDto(savedCourse));
    }
}


