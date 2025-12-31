package com.formation.service;

import com.formation.constants.AppConstants;
import com.formation.constants.UserType;
import com.formation.dto.RegistrationDto;
import com.formation.entity.*;
import com.formation.repository.*;
import com.formation.exception.BusinessException;
import com.formation.exception.ResourceNotFoundException;
import com.formation.exception.ValidationException;
import com.formation.service.util.PasswordService;
import com.formation.service.util.UserValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class RegistrationService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private TrainerRepository trainerRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordService passwordService;
    
    @Autowired
    private UserValidationService validationService;
    
    @Autowired
    private SpecialtyRepository specialtyRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    public void register(RegistrationDto registrationDto) {
        validateRegistration(registrationDto);
        
        try {
            if (UserType.ETUDIANT.equals(registrationDto.getUserType())) {
                registerStudent(registrationDto);
            } else if (UserType.FORMATEUR.equals(registrationDto.getUserType())) {
                registerTrainer(registrationDto);
            } else {
                throw new ValidationException("Type d'utilisateur invalide");
            }
        } catch (DataIntegrityViolationException e) {
            handleDataIntegrityException(e);
        }
    }
    
    private void validateRegistration(RegistrationDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new ValidationException("Ce nom d'utilisateur est déjà utilisé");
        }
        
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ValidationException("Cet email est déjà utilisé");
        }
        
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new ValidationException("Les mots de passe ne correspondent pas");
        }
    }
    
    private void handleDataIntegrityException(DataIntegrityViolationException e) {
        String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        
        if (errorMessage.contains("username") || errorMessage.contains("uk_username")) {
            throw new ValidationException("Ce nom d'utilisateur est déjà utilisé");
        } else if (errorMessage.contains("email") || errorMessage.contains("uk_email")) {
            throw new ValidationException("Cet email est déjà utilisé");
        } else if (errorMessage.contains("matricule")) {
            throw new ValidationException("Ce matricule est déjà utilisé");
        } else {
            throw new BusinessException("Erreur lors de la création du compte. Veuillez vérifier que le nom d'utilisateur, l'email et le matricule sont uniques.");
        }
    }
    
    private void registerStudent(RegistrationDto dto) {
        Student student = new Student();
        student.setUsername(dto.getUsername());
        student.setPassword(passwordService.encode(dto.getPassword()));
        student.setEmail(dto.getEmail());
        student.setNom(dto.getNom());
        student.setPrenom(dto.getPrenom());
        
        // Gérer le matricule : vérifier l'unicité si fourni, sinon générer un matricule unique
        String matricule;
        if (dto.getMatricule() != null && !dto.getMatricule().trim().isEmpty()) {
            matricule = dto.getMatricule().trim();
            // Vérifier si le matricule existe déjà
            if (studentRepository.findByMatricule(matricule).isPresent()) {
                throw new ValidationException("Ce matricule est déjà utilisé: " + matricule);
            }
        } else {
            // Générer un matricule unique
            matricule = generateUniqueMatricule();
        }
        student.setMatricule(matricule);
        
        student.setDateInscription(LocalDate.now());
        student.setEnabled(true);
        
        assignSpecialty(student, dto);
        assignGroup(student, dto);
        
        // Assigner le rôle ETUDIANT
        Set<Role> roles = new HashSet<>();
        Role etudiantRole = roleRepository.findByName(Role.RoleType.ETUDIANT)
            .orElseThrow(() -> new ResourceNotFoundException("Rôle ETUDIANT non trouvé"));
        roles.add(etudiantRole);
        student.setRoles(roles);
        
        studentRepository.save(student);
    }
    
    private void registerTrainer(RegistrationDto dto) {
        Trainer trainer = new Trainer();
        trainer.setUsername(dto.getUsername());
        trainer.setPassword(passwordService.encode(dto.getPassword()));
        trainer.setEmail(dto.getEmail());
        trainer.setNom(dto.getNom());
        trainer.setPrenom(dto.getPrenom());
        trainer.setSpecialite(dto.getSpecialite() != null ? dto.getSpecialite() : AppConstants.DEFAULT_SPECIALTY_NAME);
        trainer.setEnabled(true);
        
        // Créer automatiquement une spécialité à partir de la spécialité du formateur
        if (dto.getSpecialite() != null && !dto.getSpecialite().trim().isEmpty()) {
            String specialtyName = dto.getSpecialite().trim();
            
            // Vérifier si la spécialité existe déjà
            Specialty existingSpecialty = specialtyRepository.findByNom(specialtyName).orElse(null);
            if (existingSpecialty == null) {
                // Créer une nouvelle spécialité
                Specialty newSpecialty = new Specialty();
                newSpecialty.setNom(specialtyName);
                newSpecialty.setDescription(AppConstants.DEFAULT_SPECIALTY_DESCRIPTION);
                specialtyRepository.save(newSpecialty);
            }
        }
        
        // Assigner le rôle FORMATEUR
        Set<Role> roles = new HashSet<>();
        Role formateurRole = roleRepository.findByName(Role.RoleType.FORMATEUR)
            .orElseThrow(() -> new ResourceNotFoundException("Rôle FORMATEUR non trouvé"));
        roles.add(formateurRole);
        trainer.setRoles(roles);
        
        trainerRepository.save(trainer);
    }
    
    /**
     * Génère un matricule unique en vérifiant qu'il n'existe pas déjà
     */
    private String generateUniqueMatricule() {
        String matricule;
        int attempts = 0;
        do {
            // Générer un matricule avec timestamp + un nombre aléatoire pour éviter les collisions
            matricule = AppConstants.MATRICULE_PREFIX + System.currentTimeMillis() + (attempts > 0 ? "_" + attempts : "");
            attempts++;
            
            if (attempts >= AppConstants.MAX_MATRICULE_GENERATION_ATTEMPTS) {
                throw new BusinessException("Impossible de générer un matricule unique après " + AppConstants.MAX_MATRICULE_GENERATION_ATTEMPTS + " tentatives");
            }
        } while (studentRepository.existsByMatricule(matricule));
        
        return matricule;
    }
    
    private void assignSpecialty(Student student, RegistrationDto dto) {
        if (dto.getSpecialtyId() != null) {
            Specialty specialty = specialtyRepository.findById(dto.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Spécialité non trouvée"));
            student.setSpecialty(specialty);
        }
    }
    
    private void assignGroup(Student student, RegistrationDto dto) {
        if (dto.getGroupId() != null) {
            try {
                Long groupId = Long.parseLong(dto.getGroupId().toString());
                Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ResourceNotFoundException("Groupe non trouvé"));
                student.setGroup(group);
            } catch (NumberFormatException e) {
                // Si groupId n'est pas un nombre valide, créer ou trouver par nom
                findOrCreateGroupByName(student, dto.getGroupName());
            }
        } else if (dto.getGroupName() != null && !dto.getGroupName().trim().isEmpty()) {
            findOrCreateGroupByName(student, dto.getGroupName());
        }
    }
    
    private void findOrCreateGroupByName(Student student, String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            return;
        }
        
        String trimmedName = groupName.trim();
        Group existingGroup = groupRepository.findByNom(trimmedName).orElse(null);
        
        if (existingGroup != null) {
            student.setGroup(existingGroup);
        } else {
            Group newGroup = createNewGroup(trimmedName);
            student.setGroup(newGroup);
        }
    }
    
    private Group createNewGroup(String groupName) {
        Group newGroup = new Group();
        newGroup.setNom(groupName);
        newGroup.setDescription(AppConstants.DEFAULT_GROUP_DESCRIPTION);
        return groupRepository.save(newGroup);
    }
}

