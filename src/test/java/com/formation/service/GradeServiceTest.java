package com.formation.service;

import com.formation.entity.Course;
import com.formation.entity.Grade;
import com.formation.entity.Student;
import com.formation.exception.ResourceNotFoundException;
import com.formation.repository.CourseRepository;
import com.formation.repository.GradeRepository;
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
class GradeServiceTest {
    
    @Mock
    private GradeRepository gradeRepository;
    
    @Mock
    private StudentRepository studentRepository;
    
    @Mock
    private CourseRepository courseRepository;
    
    @InjectMocks
    private GradeService gradeService;
    
    private Student testStudent;
    private Course testCourse;
    private Grade testGrade;
    
    @BeforeEach
    void setUp() {
        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setNom("Doe");
        testStudent.setPrenom("John");
        
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setCode("COURSE001");
        testCourse.setTitre("Test Course");
        
        testGrade = new Grade();
        testGrade.setId(1L);
        testGrade.setStudent(testStudent);
        testGrade.setCours(testCourse);
        testGrade.setValeur(15.0);
        testGrade.setCommentaire("Excellent");
    }
    
    @Test
    void testSaveOrUpdate_WhenNewGrade_ShouldCreate() {
        // Given
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(gradeRepository.findByStudentIdAndCoursId(1L, 1L)).thenReturn(Optional.empty());
        when(gradeRepository.save(any(Grade.class))).thenReturn(testGrade);
        
        // When
        Grade result = gradeService.saveOrUpdate(1L, 1L, 15.0, "Excellent");
        
        // Then
        assertNotNull(result);
        assertEquals(15.0, result.getValeur());
        assertEquals("Excellent", result.getCommentaire());
        verify(gradeRepository, times(1)).save(any(Grade.class));
    }
    
    @Test
    void testSaveOrUpdate_WhenExistingGrade_ShouldUpdate() {
        // Given
        Grade existingGrade = new Grade();
        existingGrade.setId(1L);
        existingGrade.setValeur(10.0);
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(gradeRepository.findByStudentIdAndCoursId(1L, 1L)).thenReturn(Optional.of(existingGrade));
        when(gradeRepository.save(any(Grade.class))).thenReturn(existingGrade);
        
        // When
        Grade result = gradeService.saveOrUpdate(1L, 1L, 18.0, "Très bien");
        
        // Then
        assertEquals(18.0, result.getValeur());
        assertEquals("Très bien", result.getCommentaire());
        verify(gradeRepository, times(1)).save(existingGrade);
    }
    
    @Test
    void testSaveOrUpdate_WhenStudentNotFound_ShouldThrowException() {
        // Given
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            gradeService.saveOrUpdate(999L, 1L, 15.0, "Comment");
        });
        verify(gradeRepository, never()).save(any());
    }
    
    @Test
    void testSaveOrUpdate_WhenCourseNotFound_ShouldThrowException() {
        // Given
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            gradeService.saveOrUpdate(1L, 999L, 15.0, "Comment");
        });
        verify(gradeRepository, never()).save(any());
    }
    
    @Test
    void testFindByStudent_ShouldReturnGrades() {
        // Given
        List<Grade> grades = Arrays.asList(testGrade);
        when(gradeRepository.findByStudentId(1L)).thenReturn(grades);
        
        // When
        List<Grade> result = gradeService.findByStudent(1L);
        
        // Then
        assertEquals(1, result.size());
        assertEquals(15.0, result.get(0).getValeur());
        verify(gradeRepository, times(1)).findByStudentId(1L);
    }
    
    @Test
    void testFindByCourse_ShouldReturnGrades() {
        // Given
        List<Grade> grades = Arrays.asList(testGrade);
        when(gradeRepository.findByCoursId(1L)).thenReturn(grades);
        
        // When
        List<Grade> result = gradeService.findByCourse(1L);
        
        // Then
        assertEquals(1, result.size());
        verify(gradeRepository, times(1)).findByCoursId(1L);
    }
    
    @Test
    void testCalculateStudentAverage_ShouldReturnAverage() {
        // Given
        when(gradeRepository.calculateAverageByStudentId(1L)).thenReturn(15.5);
        
        // When
        Double result = gradeService.calculateStudentAverage(1L);
        
        // Then
        assertEquals(15.5, result);
        verify(gradeRepository, times(1)).calculateAverageByStudentId(1L);
    }
    
    @Test
    void testCalculateCourseSuccessRate_ShouldReturnRate() {
        // Given
        when(gradeRepository.countPassingGradesByCoursId(1L)).thenReturn(8L);
        when(gradeRepository.countTotalGradesByCoursId(1L)).thenReturn(10L);
        
        // When
        Double result = gradeService.calculateCourseSuccessRate(1L);
        
        // Then
        assertEquals(80.0, result);
        verify(gradeRepository, times(1)).countPassingGradesByCoursId(1L);
        verify(gradeRepository, times(1)).countTotalGradesByCoursId(1L);
    }
    
    @Test
    void testCalculateCourseSuccessRate_WhenNoGrades_ShouldReturnZero() {
        // Given
        when(gradeRepository.countTotalGradesByCoursId(1L)).thenReturn(0L);
        
        // When
        Double result = gradeService.calculateCourseSuccessRate(1L);
        
        // Then
        assertEquals(0.0, result);
    }
    
    @Test
    void testCalculateAverage_StaticMethod_ShouldReturnAverage() {
        // Given
        List<Grade> grades = Arrays.asList(
            createGrade(10.0),
            createGrade(15.0),
            createGrade(20.0)
        );
        
        // When
        Double result = GradeService.calculateAverage(grades);
        
        // Then
        assertEquals(15.0, result);
    }
    
    @Test
    void testCalculateAverage_WhenEmptyList_ShouldReturnZero() {
        // When
        Double result = GradeService.calculateAverage(List.of());
        
        // Then
        assertEquals(0.0, result);
    }
    
    private Grade createGrade(Double valeur) {
        Grade grade = new Grade();
        grade.setValeur(valeur);
        return grade;
    }
}

