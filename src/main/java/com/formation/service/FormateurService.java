package com.formation.service;

import com.formation.entity.*;
import com.formation.exception.BusinessException;
import com.formation.exception.ResourceNotFoundException;
import com.formation.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class FormateurService {
    
    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final GroupRepository groupRepository;
    private final StudentRepository studentRepository;
    
    @Autowired
    public FormateurService(TrainerRepository trainerRepository,
                           UserRepository userRepository,
                           CourseRepository courseRepository,
                           EnrollmentRepository enrollmentRepository,
                           GradeRepository gradeRepository,
                           GroupRepository groupRepository,
                           StudentRepository studentRepository) {
        this.trainerRepository = trainerRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.gradeRepository = gradeRepository;
        this.groupRepository = groupRepository;
        this.studentRepository = studentRepository;
    }
    
    /**
     * Récupère le formateur connecté à partir du username
     */
    public Trainer getCurrentTrainer(String username) {
        User user = userRepository.findByUsernameWithRoles(username)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé: " + username));
        
        if (user instanceof Trainer) {
            return (Trainer) user;
        }
        throw new BusinessException("L'utilisateur n'est pas un formateur");
    }
    
    /**
     * Récupère tous les cours du formateur
     */
    public List<Course> getTrainerCourses(Long trainerId) {
        return courseRepository.findByFormateurId(trainerId);
    }
    
    /**
     * Récupère tous les étudiants inscrits aux cours du formateur
     */
    public List<Student> getTrainerStudents(Long trainerId) {
        List<Course> courses = courseRepository.findByFormateurId(trainerId);
        if (courses.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> courseIds = courses.stream()
            .map(Course::getId)
            .collect(Collectors.toList());
        
        return enrollmentRepository.findByCoursIdIn(courseIds).stream()
            .map(Enrollment::getStudent)
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * Récupère tous les étudiants d'un cours spécifique du formateur
     */
    public List<Student> getStudentsByCourse(Long trainerId, Long courseId) {
        Course course = validateCourseOwnership(trainerId, courseId);
        return enrollmentRepository.findByCoursId(courseId).stream()
            .map(Enrollment::getStudent)
            .collect(Collectors.toList());
    }
    
    private Course validateCourseOwnership(Long trainerId, Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé"));
        
        if (!course.getFormateur().getId().equals(trainerId)) {
            throw new BusinessException("Ce cours ne vous appartient pas");
        }
        return course;
    }
    
    /**
     * Récupère tous les groupes avec leurs étudiants (pour les étudiants des cours du formateur)
     */
    public List<Group> getGroupsWithStudents(Long trainerId) {
        // Récupérer tous les étudiants des cours du formateur
        List<Student> trainerStudents = getTrainerStudents(trainerId);
        
        // Récupérer tous les groupes de ces étudiants
        return trainerStudents.stream()
            .filter(s -> s.getGroup() != null)
            .map(Student::getGroup)
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * Récupère tous les groupes (tous les groupes, pas seulement ceux des étudiants du formateur)
     */
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }
    
    /**
     * Récupère les étudiants d'un groupe spécifique
     */
    public List<Student> getStudentsByGroup(Long groupId) {
        return studentRepository.findByGroupId(groupId);
    }
    
    /**
     * Récupère toutes les notes des étudiants du formateur (pour ses cours)
     */
    public List<Grade> getTrainerGrades(Long trainerId) {
        List<Course> courses = courseRepository.findByFormateurId(trainerId);
        if (courses.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> courseIds = courses.stream()
            .map(Course::getId)
            .collect(Collectors.toList());
        
        return gradeRepository.findByCoursIdIn(courseIds);
    }
    
    /**
     * Récupère les notes d'un cours spécifique du formateur
     */
    public List<Grade> getGradesByCourse(Long trainerId, Long courseId) {
        validateCourseOwnership(trainerId, courseId);
        return gradeRepository.findByCoursId(courseId);
    }
    
    /**
     * Récupère un cours spécifique du formateur
     */
    public Optional<Course> getTrainerCourse(Long trainerId, Long courseId) {
        return courseRepository.findById(courseId)
            .filter(course -> course.getFormateur().getId().equals(trainerId));
    }
}

