package com.formation.service;

import com.formation.entity.Role;
import com.formation.entity.Trainer;
import com.formation.repository.RoleRepository;
import com.formation.repository.TrainerRepository;
import com.formation.service.util.PasswordService;
import com.formation.service.util.UserValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {
    
    @Mock
    private TrainerRepository trainerRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private PasswordService passwordService;
    
    @Mock
    private UserValidationService validationService;
    
    @InjectMocks
    private TrainerService trainerService;
    
    private Trainer testTrainer;
    
    @BeforeEach
    void setUp() {
        testTrainer = new Trainer();
        testTrainer.setId(1L);
        testTrainer.setUsername("trainer1");
        testTrainer.setEmail("trainer@test.com");
        testTrainer.setNom("Trainer");
        testTrainer.setPrenom("Test");
        testTrainer.setSpecialite("Java");
    }
    
    @Test
    void testFindById_WhenTrainerExists_ShouldReturnTrainer() {
        // Given
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(testTrainer));
        
        // When
        Optional<Trainer> result = trainerService.findById(1L);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("trainer1", result.get().getUsername());
        verify(trainerRepository, times(1)).findById(1L);
    }
    
    @Test
    void testFindById_WhenTrainerNotExists_ShouldReturnEmpty() {
        // Given
        when(trainerRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When
        Optional<Trainer> result = trainerService.findById(999L);
        
        // Then
        assertFalse(result.isPresent());
        verify(trainerRepository, times(1)).findById(999L);
    }
    
    @Test
    void testFindAll_ShouldReturnAllTrainers() {
        // Given
        List<Trainer> trainers = Arrays.asList(testTrainer);
        when(trainerRepository.findAll()).thenReturn(trainers);
        
        // When
        List<Trainer> result = trainerService.findAll();
        
        // Then
        assertEquals(1, result.size());
        verify(trainerRepository, times(1)).findAll();
    }
    
    @Test
    void testSave_WhenNewTrainer_ShouldEncodePassword() {
        // Given
        Trainer newTrainer = new Trainer();
        newTrainer.setUsername("newtrainer");
        newTrainer.setPassword("plainPassword");
        
        Role formateurRole = new Role();
        formateurRole.setName(Role.RoleType.FORMATEUR);
        
        doNothing().when(validationService).validateUserUniqueness(any(Trainer.class));
        when(passwordService.encode("plainPassword")).thenReturn("encodedPassword");
        when(roleRepository.findByName(Role.RoleType.FORMATEUR)).thenReturn(Optional.of(formateurRole));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(newTrainer);
        
        // When
        Trainer saved = trainerService.save(newTrainer);
        
        // Then
        verify(validationService, times(1)).validateUserUniqueness(any(Trainer.class));
        verify(passwordService, times(1)).encode("plainPassword");
        verify(trainerRepository, times(1)).save(any(Trainer.class));
        assertNotNull(saved);
    }
    
    @Test
    void testUpdate_WhenExistingTrainer_ShouldUpdate() {
        // Given
        Trainer updatedDetails = new Trainer();
        updatedDetails.setNom("Updated");
        updatedDetails.setPrenom("Trainer");
        updatedDetails.setEmail("updated@test.com");
        updatedDetails.setSpecialite("Python");
        
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(testTrainer));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);
        
        // When
        Trainer saved = trainerService.update(1L, updatedDetails);
        
        // Then
        verify(trainerRepository, times(1)).findById(1L);
        verify(trainerRepository, times(1)).save(any(Trainer.class));
        assertEquals("Updated", testTrainer.getNom());
    }
    
    @Test
    void testDelete_ShouldCallRepository() {
        // Given
        doNothing().when(trainerRepository).deleteById(1L);
        
        // When
        trainerService.delete(1L);
        
        // Then
        verify(trainerRepository, times(1)).deleteById(1L);
    }
}

