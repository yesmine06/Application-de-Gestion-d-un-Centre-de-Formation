package com.formation.service;

import com.formation.entity.Course;
import com.formation.entity.Trainer;
import com.formation.repository.CourseRepository;
import com.formation.repository.TrainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
    
    @Mock
    private CourseRepository courseRepository;
    
    @Mock
    private TrainerRepository trainerRepository;
    
    @InjectMocks
    private CourseService courseService;
    
    private Course testCourse;
    private Trainer testTrainer;
    
    @BeforeEach
    void setUp() {
        testTrainer = new Trainer();
        testTrainer.setId(1L);
        testTrainer.setNom("Trainer");
        testTrainer.setPrenom("Test");
        
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setCode("COURSE001");
        testCourse.setTitre("Test Course");
        testCourse.setDescription("Description");
        testCourse.setFormateur(testTrainer);
    }
    
    @Test
    void testFindById_WhenCourseExists_ShouldReturnCourse() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        
        // When
        Optional<Course> result = courseService.findById(1L);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("COURSE001", result.get().getCode());
        verify(courseRepository, times(1)).findById(1L);
    }
    
    @Test
    void testFindById_WhenCourseNotExists_ShouldReturnEmpty() {
        // Given
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When
        Optional<Course> result = courseService.findById(999L);
        
        // Then
        assertFalse(result.isPresent());
        verify(courseRepository, times(1)).findById(999L);
    }
    
    @Test
    void testSave_WhenValidCourse_ShouldSave() {
        // Given
        Course newCourse = new Course();
        newCourse.setCode("COURSE002");
        newCourse.setTitre("New Course");
        newCourse.setFormateur(testTrainer);
        
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(testTrainer));
        when(courseRepository.save(any(Course.class))).thenReturn(newCourse);
        
        // When
        Course saved = courseService.save(newCourse);
        
        // Then
        verify(courseRepository, times(1)).save(any(Course.class));
        assertNotNull(saved);
    }
    
    @Test
    void testDelete_ShouldCallRepository() {
        // Given
        doNothing().when(courseRepository).deleteById(1L);
        
        // When
        courseService.delete(1L);
        
        // Then
        verify(courseRepository, times(1)).deleteById(1L);
    }
}

