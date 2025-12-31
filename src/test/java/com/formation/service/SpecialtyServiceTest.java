package com.formation.service;

import com.formation.entity.Specialty;
import com.formation.exception.ResourceNotFoundException;
import com.formation.repository.SpecialtyRepository;
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
class SpecialtyServiceTest {
    
    @Mock
    private SpecialtyRepository specialtyRepository;
    
    @InjectMocks
    private SpecialtyService specialtyService;
    
    private Specialty testSpecialty;
    
    @BeforeEach
    void setUp() {
        testSpecialty = new Specialty();
        testSpecialty.setId(1L);
        testSpecialty.setNom("Informatique");
        testSpecialty.setDescription("Spécialité en informatique");
    }
    
    @Test
    void testFindById_WhenSpecialtyExists_ShouldReturnSpecialty() {
        // Given
        when(specialtyRepository.findById(1L)).thenReturn(Optional.of(testSpecialty));
        
        // When
        Optional<Specialty> result = specialtyService.findById(1L);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("Informatique", result.get().getNom());
        verify(specialtyRepository, times(1)).findById(1L);
    }
    
    @Test
    void testFindById_WhenSpecialtyNotExists_ShouldReturnEmpty() {
        // Given
        when(specialtyRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When
        Optional<Specialty> result = specialtyService.findById(999L);
        
        // Then
        assertFalse(result.isPresent());
        verify(specialtyRepository, times(1)).findById(999L);
    }
    
    @Test
    void testFindAll_ShouldReturnAllSpecialties() {
        // Given
        List<Specialty> specialties = Arrays.asList(testSpecialty);
        when(specialtyRepository.findAll()).thenReturn(specialties);
        
        // When
        List<Specialty> result = specialtyService.findAll();
        
        // Then
        assertEquals(1, result.size());
        verify(specialtyRepository, times(1)).findAll();
    }
    
    @Test
    void testSave_WhenValidSpecialty_ShouldSave() {
        // Given
        Specialty newSpecialty = new Specialty();
        newSpecialty.setNom("Mathématiques");
        newSpecialty.setDescription("Spécialité en mathématiques");
        
        when(specialtyRepository.save(any(Specialty.class))).thenReturn(newSpecialty);
        
        // When
        Specialty saved = specialtyService.save(newSpecialty);
        
        // Then
        verify(specialtyRepository, times(1)).save(any(Specialty.class));
        assertNotNull(saved);
        assertEquals("Mathématiques", saved.getNom());
    }
    
    @Test
    void testUpdate_WhenSpecialtyExists_ShouldUpdate() {
        // Given
        Specialty updatedDetails = new Specialty();
        updatedDetails.setNom("Informatique Avancée");
        updatedDetails.setDescription("Description mise à jour");
        
        when(specialtyRepository.findById(1L)).thenReturn(Optional.of(testSpecialty));
        when(specialtyRepository.save(any(Specialty.class))).thenReturn(testSpecialty);
        
        // When
        Specialty result = specialtyService.update(1L, updatedDetails);
        
        // Then
        verify(specialtyRepository, times(1)).findById(1L);
        verify(specialtyRepository, times(1)).save(testSpecialty);
        assertEquals("Informatique Avancée", testSpecialty.getNom());
        assertEquals("Description mise à jour", testSpecialty.getDescription());
    }
    
    @Test
    void testUpdate_WhenSpecialtyNotExists_ShouldThrowException() {
        // Given
        Specialty updatedDetails = new Specialty();
        updatedDetails.setNom("Updated");
        
        when(specialtyRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            specialtyService.update(999L, updatedDetails);
        });
        verify(specialtyRepository, never()).save(any());
    }
    
    @Test
    void testDelete_ShouldCallRepository() {
        // Given
        doNothing().when(specialtyRepository).deleteById(1L);
        
        // When
        specialtyService.delete(1L);
        
        // Then
        verify(specialtyRepository, times(1)).deleteById(1L);
    }
}

