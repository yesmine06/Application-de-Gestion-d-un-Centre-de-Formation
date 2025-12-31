package com.formation.service;

import com.formation.constants.UserType;
import com.formation.dto.RegistrationDto;
import com.formation.entity.Role;
import com.formation.entity.Specialty;
import com.formation.exception.ValidationException;
import com.formation.repository.*;
import com.formation.service.util.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private StudentRepository studentRepository;
    
    @Mock
    private TrainerRepository trainerRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private SpecialtyRepository specialtyRepository;
    
    @Mock
    private GroupRepository groupRepository;
    
    @Mock
    private PasswordService passwordService;
    
    @InjectMocks
    private RegistrationService registrationService;
    
    private RegistrationDto registrationDto;
    private Role etudiantRole;
    private Role formateurRole;
    
    @BeforeEach
    void setUp() {
        registrationDto = new RegistrationDto();
        registrationDto.setUsername("testuser");
        registrationDto.setEmail("test@test.com");
        registrationDto.setPassword("password123");
        registrationDto.setConfirmPassword("password123");
        registrationDto.setNom("Test");
        registrationDto.setPrenom("User");
        
        etudiantRole = new Role();
        etudiantRole.setName(Role.RoleType.ETUDIANT);
        
        formateurRole = new Role();
        formateurRole.setName(Role.RoleType.FORMATEUR);
    }
    
    @Test
    void testRegister_WhenStudent_ShouldCreateStudent() {
        // Given
        registrationDto.setUserType(UserType.ETUDIANT);
        registrationDto.setMatricule("MAT001");
        
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(studentRepository.findByMatricule("MAT001")).thenReturn(Optional.empty());
        when(passwordService.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(Role.RoleType.ETUDIANT)).thenReturn(Optional.of(etudiantRole));
        when(specialtyRepository.findById(any())).thenReturn(Optional.empty());
        when(groupRepository.findById(any())).thenReturn(Optional.empty());
        when(studentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        assertDoesNotThrow(() -> registrationService.register(registrationDto));
        
        // Then
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, times(1)).existsByEmail("test@test.com");
        verify(passwordService, times(1)).encode("password123");
        verify(studentRepository, times(1)).save(any());
    }
    
    @Test
    void testRegister_WhenTrainer_ShouldCreateTrainer() {
        // Given
        registrationDto.setUserType(UserType.FORMATEUR);
        registrationDto.setSpecialite("Java");
        
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordService.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(Role.RoleType.FORMATEUR)).thenReturn(Optional.of(formateurRole));
        when(specialtyRepository.findByNom("Java")).thenReturn(Optional.empty());
        when(specialtyRepository.save(any(Specialty.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(trainerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        assertDoesNotThrow(() -> registrationService.register(registrationDto));
        
        // Then
        verify(trainerRepository, times(1)).save(any());
        verify(specialtyRepository, times(1)).save(any(Specialty.class));
    }
    
    @Test
    void testRegister_WhenUsernameExists_ShouldThrowException() {
        // Given
        registrationDto.setUserType(UserType.ETUDIANT);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            registrationService.register(registrationDto);
        });
        
        assertEquals("Ce nom d'utilisateur est déjà utilisé", exception.getMessage());
        verify(studentRepository, never()).save(any());
    }
    
    @Test
    void testRegister_WhenEmailExists_ShouldThrowException() {
        // Given
        registrationDto.setUserType(UserType.ETUDIANT);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);
        
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            registrationService.register(registrationDto);
        });
        
        assertEquals("Cet email est déjà utilisé", exception.getMessage());
        verify(studentRepository, never()).save(any());
    }
    
    @Test
    void testRegister_WhenPasswordsDoNotMatch_ShouldThrowException() {
        // Given
        registrationDto.setUserType(UserType.ETUDIANT);
        registrationDto.setPassword("password123");
        registrationDto.setConfirmPassword("different");
        
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            registrationService.register(registrationDto);
        });
        
        assertEquals("Les mots de passe ne correspondent pas", exception.getMessage());
        verify(studentRepository, never()).save(any());
    }
    
    @Test
    void testRegister_WhenInvalidUserType_ShouldThrowException() {
        // Given
        registrationDto.setUserType(null);
        
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            registrationService.register(registrationDto);
        });
        
        assertEquals("Type d'utilisateur invalide", exception.getMessage());
    }
    
    @Test
    void testRegister_WhenMatriculeExists_ShouldThrowException() {
        // Given
        registrationDto.setUserType(UserType.ETUDIANT);
        registrationDto.setMatricule("MAT001");
        
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(studentRepository.findByMatricule("MAT001")).thenReturn(Optional.of(new com.formation.entity.Student()));
        
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            registrationService.register(registrationDto);
        });
        
        assertTrue(exception.getMessage().contains("matricule"));
        verify(studentRepository, never()).save(any());
    }
    
    @Test
    void testRegister_WhenDataIntegrityViolation_ShouldHandleException() {
        // Given
        registrationDto.setUserType(UserType.ETUDIANT);
        
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(studentRepository.findByMatricule(anyString())).thenReturn(Optional.empty());
        when(passwordService.encode(anyString())).thenReturn("encoded");
        when(roleRepository.findByName(Role.RoleType.ETUDIANT)).thenReturn(Optional.of(etudiantRole));
        when(studentRepository.save(any())).thenThrow(
            new DataIntegrityViolationException("Duplicate entry 'testuser' for key 'uk_username'")
        );
        
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            registrationService.register(registrationDto);
        });
        
        assertTrue(exception.getMessage().contains("nom d'utilisateur"));
    }
}

