package com.formation.service;

import com.formation.entity.Course;
import com.formation.entity.Trainer;
import com.formation.entity.Session;
import com.formation.entity.Group;
import com.formation.exception.ResourceNotFoundException;
import com.formation.repository.CourseRepository;
import com.formation.repository.TrainerRepository;
import com.formation.repository.SessionRepository;
import com.formation.repository.GroupRepository;
import com.formation.service.util.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CourseService extends EntityService<Course, Long> {
    
    private final TrainerRepository trainerRepository;
    private final SessionRepository sessionRepository;
    private final GroupRepository groupRepository;
    
    @Autowired
    public CourseService(CourseRepository courseRepository,
                        TrainerRepository trainerRepository,
                        SessionRepository sessionRepository,
                        GroupRepository groupRepository) {
        super(courseRepository);
        this.trainerRepository = trainerRepository;
        this.sessionRepository = sessionRepository;
        this.groupRepository = groupRepository;
    }
    
    private CourseRepository getCourseRepository() {
        return (CourseRepository) repository;
    }
    
    public Page<Course> findAll(Pageable pageable) {
        return getCourseRepository().findAll(pageable);
    }
    
    public Optional<Course> findByCode(String code) {
        return getCourseRepository().findByCode(code);
    }
    
    public Course save(Course course) {
        loadRelations(course);
        return repository.save(course);
    }
    
    public Course update(Long id, Course courseDetails) {
        Course course = findByIdOrThrow(id, "Cours");
        
        updateBasicFields(course, courseDetails);
        loadRelations(course);
        
        return repository.save(course);
    }
    
    private void updateBasicFields(Course course, Course courseDetails) {
        course.setCode(courseDetails.getCode());
        course.setTitre(courseDetails.getTitre());
        course.setDescription(courseDetails.getDescription());
    }
    
    private void loadRelations(Course course) {
        if (course.getFormateur() != null && course.getFormateur().getId() != null) {
            Trainer trainer = trainerRepository.findById(course.getFormateur().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Formateur non trouvé"));
            course.setFormateur(trainer);
        }
        
        if (course.getSession() != null && course.getSession().getId() != null) {
            Session session = sessionRepository.findById(course.getSession().getId())
                .orElse(null);
            course.setSession(session);
        }
    }
    
    public List<Course> findByTrainer(Long trainerId) {
        return getCourseRepository().findByFormateurId(trainerId);
    }
    
    public List<Course> findBySession(Long sessionId) {
        return getCourseRepository().findBySessionId(sessionId);
    }
    
    public Course addGroupToCourse(Long courseId, Long groupId) {
        Course course = findByIdOrThrow(courseId, "Cours");
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Groupe non trouvé"));
        
        if (!course.getGroups().contains(group)) {
            course.getGroups().add(group);
        }
        
        return repository.save(course);
    }
    
    public Course removeGroupFromCourse(Long courseId, Long groupId) {
        Course course = findByIdOrThrow(courseId, "Cours");
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Groupe non trouvé"));
        
        course.getGroups().remove(group);
        return repository.save(course);
    }
    
    /**
     * Supprime un cours par ID
     */
    public void delete(Long id) {
        deleteById(id);
    }
}


