package com.formation.service;

import com.formation.entity.Course;
import com.formation.entity.Enrollment;
import com.formation.entity.Student;
import com.formation.entity.Trainer;
import com.formation.exception.BusinessException;
import com.formation.exception.ResourceNotFoundException;
import com.formation.repository.CourseRepository;
import com.formation.repository.EnrollmentRepository;
import com.formation.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {
    
    @Mock
    private EnrollmentRepository enrollmentRepository;
    
    @Mock
    private StudentRepository studentRepository;
    
    @Mock
    private CourseRepository courseRepository;
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private EnrollmentService enrollmentService;
    
    private Student testStudent;
    private Course testCourse;
    private Trainer testTrainer;
    private Enrollment testEnrollment;
    
    @BeforeEach
    void setUp() {
        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setNom("Doe");
        testStudent.setPrenom("John");
        testStudent.setEmail("john.doe@test.com");
        
        testTrainer = new Trainer();
        testTrainer.setId(1L);
        testTrainer.setNom("Smith");
        testTrainer.setPrenom("Jane");
        testTrainer.setEmail("jane.smith@test.com");
        
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setCode("COURSE001");
        testCourse.setTitre("Test Course");
        testCourse.setFormateur(testTrainer);
        
        testEnrollment = new Enrollment();
        testEnrollment.setId(1L);
        testEnrollment.setStudent(testStudent);
        testEnrollment.setCours(testCourse);
    }
    
    @Test
    void testEnroll_WhenValid_ShouldCreateEnrollment() {
        // Given
        when(enrollmentRepository.existsByStudentIdAndCoursId(1L, 1L)).thenReturn(false);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);
        
        // When
        Enrollment result = enrollmentService.enroll(1L, 1L);
        
        // Then
        assertNotNull(result);
        assertEquals(testStudent, result.getStudent());
        assertEquals(testCourse, result.getCours());
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
        verify(emailService, times(1)).sendEnrollmentNotification(anyString(), anyString(), anyString());
        verify(emailService, times(1)).notifyTrainer(anyString(), anyString(), anyString(), anyString(), eq(true));
    }
    
    @Test
    void testEnroll_WhenAlreadyEnrolled_ShouldThrowException() {
        // Given
        when(enrollmentRepository.existsByStudentIdAndCoursId(1L, 1L)).thenReturn(true);
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            enrollmentService.enroll(1L, 1L);
        });
        verify(enrollmentRepository, never()).save(any());
        verify(emailService, never()).sendEnrollmentNotification(anyString(), anyString(), anyString());
    }
    
    @Test
    void testEnroll_WhenStudentNotFound_ShouldThrowException() {
        // Given
        when(enrollmentRepository.existsByStudentIdAndCoursId(999L, 1L)).thenReturn(false);
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            enrollmentService.enroll(999L, 1L);
        });
        verify(enrollmentRepository, never()).save(any());
    }
    
    @Test
    void testEnroll_WhenCourseNotFound_ShouldThrowException() {
        // Given
        when(enrollmentRepository.existsByStudentIdAndCoursId(1L, 999L)).thenReturn(false);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            enrollmentService.enroll(1L, 999L);
        });
        verify(enrollmentRepository, never()).save(any());
    }
    
    @Test
    void testUnenroll_WhenValid_ShouldDeleteEnrollment() {
        // Given
        when(enrollmentRepository.findByStudentIdAndCoursId(1L, 1L))
            .thenReturn(Optional.of(testEnrollment));
        doNothing().when(enrollmentRepository).delete(any(Enrollment.class));
        
        // When
        enrollmentService.unenroll(1L, 1L);
        
        // Then
        verify(enrollmentRepository, times(1)).delete(testEnrollment);
        verify(emailService, times(1)).sendUnenrollmentNotification(anyString(), anyString(), anyString());
        verify(emailService, times(1)).notifyTrainer(anyString(), anyString(), anyString(), anyString(), eq(false));
    }
    
    @Test
    void testUnenroll_WhenEnrollmentNotFound_ShouldThrowException() {
        // Given
        when(enrollmentRepository.findByStudentIdAndCoursId(1L, 1L))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            enrollmentService.unenroll(1L, 1L);
        });
        verify(enrollmentRepository, never()).delete(any());
    }
    
    @Test
    void testFindByStudent_ShouldReturnEnrollments() {
        // Given
        List<Enrollment> enrollments = Arrays.asList(testEnrollment);
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(enrollments);
        
        // When
        List<Enrollment> result = enrollmentService.findByStudent(1L);
        
        // Then
        assertEquals(1, result.size());
        verify(enrollmentRepository, times(1)).findByStudentId(1L);
    }
    
    @Test
    void testFindByCourse_ShouldReturnEnrollments() {
        // Given
        List<Enrollment> enrollments = Arrays.asList(testEnrollment);
        when(enrollmentRepository.findByCoursId(1L)).thenReturn(enrollments);
        
        // When
        List<Enrollment> result = enrollmentService.findByCourse(1L);
        
        // Then
        assertEquals(1, result.size());
        verify(enrollmentRepository, times(1)).findByCoursId(1L);
    }
    
    @Test
    void testEnroll_WhenCourseWithoutTrainer_ShouldNotNotifyTrainer() {
        // Given
        Course courseWithoutTrainer = new Course();
        courseWithoutTrainer.setId(2L);
        courseWithoutTrainer.setTitre("Course Without Trainer");
        courseWithoutTrainer.setFormateur(null);
        
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(testStudent);
        enrollment.setCours(courseWithoutTrainer);
        
        when(enrollmentRepository.existsByStudentIdAndCoursId(1L, 2L)).thenReturn(false);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(courseWithoutTrainer));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);
        
        // When
        enrollmentService.enroll(1L, 2L);
        
        // Then
        verify(emailService, times(1)).sendEnrollmentNotification(anyString(), anyString(), anyString());
        verify(emailService, never()).notifyTrainer(anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }
}

