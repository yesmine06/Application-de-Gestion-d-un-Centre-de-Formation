package com.formation.service;

import com.formation.entity.*;
import com.formation.exception.BusinessException;
import com.formation.exception.ResourceNotFoundException;
import com.formation.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class EtudiantService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    /**
     * Récupère l'étudiant connecté à partir du username
     */
    public Student getCurrentStudent(String username) {
        User user = userRepository.findByUsernameWithRoles(username)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé: " + username));
        
        if (user instanceof Student) {
            return (Student) user;
        }
        throw new BusinessException("L'utilisateur n'est pas un étudiant");
    }
    
    /**
     * Récupère tous les cours de l'étudiant (ceux auxquels il est inscrit)
     */
    public List<Course> getStudentCourses(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        return enrollments.stream()
            .map(Enrollment::getCours)
            .toList();
    }
    
    /**
     * Récupère toutes les notes de l'étudiant
     */
    public List<Grade> getStudentGrades(Long studentId) {
        return gradeRepository.findByStudentId(studentId);
    }
    
    /**
     * Récupère les notes d'un cours spécifique pour l'étudiant
     */
    public Optional<Grade> getGradeByCourse(Long studentId, Long courseId) {
        return gradeRepository.findByStudentIdAndCoursId(studentId, courseId);
    }
    
    /**
     * Vérifie si l'étudiant est inscrit à un cours
     */
    public boolean isEnrolledInCourse(Long studentId, Long courseId) {
        return enrollmentRepository.existsByStudentIdAndCoursId(studentId, courseId);
    }
    
    /**
     * Récupère tous les cours disponibles (non inscrits) pour l'étudiant
     */
    public List<Course> getAvailableCourses(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        Set<Long> enrolledCourseIds = enrollments.stream()
            .map(e -> e.getCours().getId())
            .collect(Collectors.toSet());
        
        return courseRepository.findAll().stream()
            .filter(course -> !enrolledCourseIds.contains(course.getId()))
            .collect(Collectors.toList());
    }
}

