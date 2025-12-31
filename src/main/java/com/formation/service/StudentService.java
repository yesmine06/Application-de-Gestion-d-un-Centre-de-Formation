package com.formation.service;

import com.formation.entity.Student;
import com.formation.entity.Specialty;
import com.formation.entity.Group;
import com.formation.exception.ResourceNotFoundException;
import com.formation.exception.ValidationException;
import com.formation.repository.StudentRepository;
import com.formation.repository.SpecialtyRepository;
import com.formation.repository.GroupRepository;
import com.formation.service.util.EntityService;
import com.formation.service.util.PasswordService;
import com.formation.service.util.UserValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentService extends EntityService<Student, Long> {
    
    private final SpecialtyRepository specialtyRepository;
    private final GroupRepository groupRepository;
    private final PasswordService passwordService;
    private final UserValidationService validationService;
    
    @Autowired
    public StudentService(StudentRepository studentRepository,
                         SpecialtyRepository specialtyRepository,
                         GroupRepository groupRepository,
                         PasswordService passwordService,
                         UserValidationService validationService) {
        super(studentRepository);
        this.specialtyRepository = specialtyRepository;
        this.groupRepository = groupRepository;
        this.passwordService = passwordService;
        this.validationService = validationService;
    }
    
    private StudentRepository getStudentRepository() {
        return (StudentRepository) repository;
    }
    
    public Page<Student> findAll(Pageable pageable) {
        return getStudentRepository().findAll(pageable);
    }
    
    public Optional<Student> findByMatricule(String matricule) {
        return getStudentRepository().findByMatricule(matricule);
    }
    
    public Student save(Student student) {
        // Générer username si nécessaire
        if (student.getUsername() == null || student.getUsername().trim().isEmpty()) {
            student.setUsername(generateUniqueUsername(student));
        }
        
        // Validation
        validationService.validateUserUniqueness(student);
        validateMatriculeUnique(student);
        
        // Gestion du mot de passe
        if (student.getId() == null) {
            student.setPassword(passwordService.encodeOrDefault(student.getPassword()));
        } else if (student.getPassword() != null && !student.getPassword().isEmpty()) {
            student.setPassword(passwordService.encode(student.getPassword()));
        }
        
        // Valeurs par défaut
        if (student.getDateInscription() == null) {
            student.setDateInscription(LocalDate.now());
        }
        student.setEnabled(true);
        
        return repository.save(student);
    }
    
    private void validateMatriculeUnique(Student student) {
        if (student.getMatricule() == null || student.getMatricule().trim().isEmpty()) {
            return;
        }
        
        getStudentRepository().findByMatricule(student.getMatricule()).ifPresent(existing -> {
            if (student.getId() == null || !existing.getId().equals(student.getId())) {
                throw new ValidationException("Ce matricule est déjà utilisé: " + student.getMatricule());
            }
        });
    }
    
    private String generateUniqueUsername(Student student) {
        String baseUsername = extractBaseUsername(student);
        return ensureUniqueUsername(baseUsername);
    }
    
    private String extractBaseUsername(Student student) {
        if (student.getMatricule() != null && !student.getMatricule().trim().isEmpty()) {
            return student.getMatricule().toLowerCase().replaceAll("[^a-z0-9]", "");
        }
        if (student.getEmail() != null && !student.getEmail().trim().isEmpty()) {
            return student.getEmail().split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        }
        return "etudiant" + System.currentTimeMillis();
    }
    
    private String ensureUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int suffix = 1;
        while (getStudentRepository().existsByUsername(username)) {
            username = baseUsername + suffix;
            suffix++;
        }
        return username;
    }
    
    public Student update(Long id, Student studentDetails) {
        Student student = findByIdOrThrow(id, "Étudiant");
        
        updateBasicFields(student, studentDetails);
        updatePassword(student, studentDetails);
        updateRelations(student, studentDetails);
        
        return repository.save(student);
    }
    
    private void updateBasicFields(Student student, Student studentDetails) {
        student.setNom(studentDetails.getNom());
        student.setPrenom(studentDetails.getPrenom());
        student.setEmail(studentDetails.getEmail());
        student.setMatricule(studentDetails.getMatricule());
    }
    
    private void updatePassword(Student student, Student studentDetails) {
        if (studentDetails.getPassword() != null && !studentDetails.getPassword().isEmpty()) {
            student.setPassword(passwordService.encode(studentDetails.getPassword()));
        }
    }
    
    /**
     * Supprime un étudiant par ID
     */
    public void delete(Long id) {
        deleteById(id);
    }
    
    private void updateRelations(Student student, Student studentDetails) {
        if (studentDetails.getSpecialty() != null) {
            Specialty specialty = specialtyRepository.findById(studentDetails.getSpecialty().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Spécialité non trouvée"));
            student.setSpecialty(specialty);
        }
        
        if (studentDetails.getGroup() != null) {
            Group group = groupRepository.findById(studentDetails.getGroup().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Groupe non trouvé"));
            student.setGroup(group);
        }
    }
    
    public List<Student> findBySpecialty(Long specialtyId) {
        return getStudentRepository().findBySpecialtyId(specialtyId);
    }
    
    public List<Student> findByGroup(Long groupId) {
        return getStudentRepository().findByGroupId(groupId);
    }
}


