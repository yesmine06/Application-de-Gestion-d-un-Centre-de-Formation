package com.formation.service;

import com.formation.entity.Student;
import com.formation.repository.StudentRepository;
import com.formation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {
    
    @Mock
    private StudentRepository studentRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private StudentService studentService;
    
    private Student testStudent;
    
    @BeforeEach
    void setUp() {
        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setUsername("testuser");
        testStudent.setEmail("test@test.com");
        testStudent.setNom("Test");
        testStudent.setPrenom("User");
        testStudent.setMatricule("MAT001");
        testStudent.setDateInscription(LocalDate.now());
    }
    
    @Test
    void testFindById_WhenStudentExists_ShouldReturnStudent() {
        // Given
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        
        // When
        Optional<Student> result = studentService.findById(1L);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(studentRepository, times(1)).findById(1L);
    }
    
    @Test
    void testFindById_WhenStudentNotExists_ShouldReturnEmpty() {
        // Given
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When
        Optional<Student> result = studentService.findById(999L);
        
        // Then
        assertFalse(result.isPresent());
        verify(studentRepository, times(1)).findById(999L);
    }
    
    @Test
    void testSave_WhenNewStudent_ShouldEncodePassword() {
        // Given
        Student newStudent = new Student();
        newStudent.setUsername("newuser");
        newStudent.setEmail("new@test.com");
        newStudent.setNom("New");
        newStudent.setPrenom("Student");
        newStudent.setMatricule("MAT002");
        newStudent.setPassword("plainPassword");
        
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(studentRepository.findByMatricule(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(studentRepository.save(any(Student.class))).thenReturn(newStudent);
        
        // When
        Student saved = studentService.save(newStudent);
        
        // Then
        verify(passwordEncoder, times(1)).encode("plainPassword");
        verify(studentRepository, times(1)).save(any(Student.class));
        assertNotNull(saved.getDateInscription());
    }
    
    @Test
    void testDelete_ShouldCallRepository() {
        // Given
        doNothing().when(studentRepository).deleteById(1L);
        
        // When
        studentService.delete(1L);
        
        // Then
        verify(studentRepository, times(1)).deleteById(1L);
    }
}

