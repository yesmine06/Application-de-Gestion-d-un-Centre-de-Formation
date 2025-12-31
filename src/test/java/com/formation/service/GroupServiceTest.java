package com.formation.service;

import com.formation.entity.Group;
import com.formation.exception.ResourceNotFoundException;
import com.formation.repository.GroupRepository;
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
class GroupServiceTest {
    
    @Mock
    private GroupRepository groupRepository;
    
    @InjectMocks
    private GroupService groupService;
    
    private Group testGroup;
    
    @BeforeEach
    void setUp() {
        testGroup = new Group();
        testGroup.setId(1L);
        testGroup.setNom("Groupe A");
        testGroup.setDescription("Description du groupe A");
    }
    
    @Test
    void testFindById_WhenGroupExists_ShouldReturnGroup() {
        // Given
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        
        // When
        Optional<Group> result = groupService.findById(1L);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("Groupe A", result.get().getNom());
        verify(groupRepository, times(1)).findById(1L);
    }
    
    @Test
    void testFindById_WhenGroupNotExists_ShouldReturnEmpty() {
        // Given
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When
        Optional<Group> result = groupService.findById(999L);
        
        // Then
        assertFalse(result.isPresent());
        verify(groupRepository, times(1)).findById(999L);
    }
    
    @Test
    void testFindAll_ShouldReturnAllGroups() {
        // Given
        List<Group> groups = Arrays.asList(testGroup);
        when(groupRepository.findAll()).thenReturn(groups);
        
        // When
        List<Group> result = groupService.findAll();
        
        // Then
        assertEquals(1, result.size());
        verify(groupRepository, times(1)).findAll();
    }
    
    @Test
    void testSave_WhenValidGroup_ShouldSave() {
        // Given
        Group newGroup = new Group();
        newGroup.setNom("Groupe B");
        newGroup.setDescription("Description du groupe B");
        
        when(groupRepository.save(any(Group.class))).thenReturn(newGroup);
        
        // When
        Group saved = groupService.save(newGroup);
        
        // Then
        verify(groupRepository, times(1)).save(any(Group.class));
        assertNotNull(saved);
        assertEquals("Groupe B", saved.getNom());
    }
    
    @Test
    void testUpdate_WhenGroupExists_ShouldUpdate() {
        // Given
        Group updatedDetails = new Group();
        updatedDetails.setNom("Groupe A Modifié");
        updatedDetails.setDescription("Description mise à jour");
        
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);
        
        // When
        Group result = groupService.update(1L, updatedDetails);
        
        // Then
        verify(groupRepository, times(1)).findById(1L);
        verify(groupRepository, times(1)).save(testGroup);
        assertEquals("Groupe A Modifié", testGroup.getNom());
        assertEquals("Description mise à jour", testGroup.getDescription());
    }
    
    @Test
    void testUpdate_WhenGroupNotExists_ShouldThrowException() {
        // Given
        Group updatedDetails = new Group();
        updatedDetails.setNom("Updated");
        
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            groupService.update(999L, updatedDetails);
        });
        verify(groupRepository, never()).save(any());
    }
    
    @Test
    void testDelete_ShouldCallRepository() {
        // Given
        doNothing().when(groupRepository).deleteById(1L);
        
        // When
        groupService.delete(1L);
        
        // Then
        verify(groupRepository, times(1)).deleteById(1L);
    }
}

