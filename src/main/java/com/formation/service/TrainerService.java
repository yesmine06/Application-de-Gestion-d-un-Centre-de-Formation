package com.formation.service;

import com.formation.entity.Trainer;
import com.formation.entity.Role;
import com.formation.exception.ResourceNotFoundException;
import com.formation.repository.TrainerRepository;
import com.formation.repository.RoleRepository;
import com.formation.service.util.EntityService;
import com.formation.service.util.PasswordService;
import com.formation.service.util.UserValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class TrainerService extends EntityService<Trainer, Long> {
    
    private final RoleRepository roleRepository;
    private final PasswordService passwordService;
    private final UserValidationService validationService;
    
    @Autowired
    public TrainerService(TrainerRepository trainerRepository,
                         RoleRepository roleRepository,
                         PasswordService passwordService,
                         UserValidationService validationService) {
        super(trainerRepository);
        this.roleRepository = roleRepository;
        this.passwordService = passwordService;
        this.validationService = validationService;
    }
    
    private TrainerRepository getTrainerRepository() {
        return (TrainerRepository) repository;
    }
    
    public Trainer save(Trainer trainer) {
        // Validation
        validationService.validateUserUniqueness(trainer);
        
        // Gestion du mot de passe
        if (trainer.getPassword() != null && !trainer.getPassword().isEmpty()) {
            trainer.setPassword(passwordService.encode(trainer.getPassword()));
        }
        
        // Assigner le rôle FORMATEUR si aucun rôle n'est défini
        assignDefaultRoleIfNeeded(trainer);
        
        // Valeurs par défaut
        trainer.setEnabled(true);
        
        return repository.save(trainer);
    }
    
    private void assignDefaultRoleIfNeeded(Trainer trainer) {
        if (trainer.getRoles() == null || trainer.getRoles().isEmpty()) {
            Role formateurRole = roleRepository.findByName(Role.RoleType.FORMATEUR)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle FORMATEUR non trouvé"));
            trainer.setRoles(new HashSet<>(Set.of(formateurRole)));
        }
    }
    
    public Trainer update(Long id, Trainer trainerDetails) {
        Trainer trainer = findByIdOrThrow(id, "Formateur");
        
        updateBasicFields(trainer, trainerDetails);
        updatePassword(trainer, trainerDetails);
        
        return repository.save(trainer);
    }
    
    private void updateBasicFields(Trainer trainer, Trainer trainerDetails) {
        trainer.setNom(trainerDetails.getNom());
        trainer.setPrenom(trainerDetails.getPrenom());
        trainer.setEmail(trainerDetails.getEmail());
        trainer.setSpecialite(trainerDetails.getSpecialite());
    }
    
    private void updatePassword(Trainer trainer, Trainer trainerDetails) {
        if (trainerDetails.getPassword() != null && !trainerDetails.getPassword().isEmpty()) {
            trainer.setPassword(passwordService.encode(trainerDetails.getPassword()));
        }
    }
    
    public List<Trainer> findBySpecialite(String specialite) {
        return getTrainerRepository().findBySpecialite(specialite);
    }
    
    /**
     * Supprime un formateur par ID
     */
    public void delete(Long id) {
        deleteById(id);
    }
}


