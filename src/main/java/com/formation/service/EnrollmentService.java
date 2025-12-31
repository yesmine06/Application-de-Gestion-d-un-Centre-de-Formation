package com.formation.service;

import com.formation.entity.Enrollment;
import com.formation.entity.Student;
import com.formation.entity.Course;
import com.formation.exception.BusinessException;
import com.formation.exception.ResourceNotFoundException;
import com.formation.repository.EnrollmentRepository;
import com.formation.repository.StudentRepository;
import com.formation.repository.CourseRepository;
import com.formation.service.util.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EnrollmentService extends EntityService<Enrollment, Long> {
    
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EmailService emailService;
    
    @Autowired
    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                            StudentRepository studentRepository,
                            CourseRepository courseRepository,
                            EmailService emailService) {
        super(enrollmentRepository);
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.emailService = emailService;
    }
    
    private EnrollmentRepository getEnrollmentRepository() {
        return (EnrollmentRepository) repository;
    }
    
    public Enrollment enroll(Long studentId, Long coursId) {
        if (getEnrollmentRepository().existsByStudentIdAndCoursId(studentId, coursId)) {
            throw new BusinessException("L'étudiant est déjà inscrit à ce cours");
        }
        
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'ID: " + studentId));
        Course course = courseRepository.findById(coursId)
            .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé avec l'ID: " + coursId));
        
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCours(course);
        
        Enrollment saved = repository.save(enrollment);
        sendEnrollmentNotifications(student, course, true);
        
        return saved;
    }
    
    public void unenroll(Long studentId, Long coursId) {
        Enrollment enrollment = getEnrollmentRepository()
            .findByStudentIdAndCoursId(studentId, coursId)
            .orElseThrow(() -> new ResourceNotFoundException("Inscription non trouvée"));
        
        Student student = enrollment.getStudent();
        Course course = enrollment.getCours();
        
        repository.delete(enrollment);
        sendEnrollmentNotifications(student, course, false);
    }
    
    private void sendEnrollmentNotifications(Student student, Course course, boolean isEnrollment) {
        if (isEnrollment) {
            emailService.sendEnrollmentNotification(
                student.getEmail(),
                student.getPrenom() + " " + student.getNom(),
                course.getTitre()
            );
        } else {
            emailService.sendUnenrollmentNotification(
                student.getEmail(),
                student.getPrenom() + " " + student.getNom(),
                course.getTitre()
            );
        }
        
        if (course.getFormateur() != null) {
            emailService.notifyTrainer(
                course.getFormateur().getEmail(),
                course.getFormateur().getPrenom() + " " + course.getFormateur().getNom(),
                student.getPrenom() + " " + student.getNom(),
                course.getTitre(),
                isEnrollment
            );
        }
    }
    
    public List<Enrollment> findByStudent(Long studentId) {
        return getEnrollmentRepository().findByStudentId(studentId);
    }
    
    public List<Enrollment> findByCourse(Long coursId) {
        return getEnrollmentRepository().findByCoursId(coursId);
    }
    
    /**
     * Supprime une inscription par ID
     */
    public void delete(Long id) {
        deleteById(id);
    }
}


