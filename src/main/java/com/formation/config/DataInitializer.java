package com.formation.config;

import com.formation.entity.*;
import com.formation.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;

    public DataInitializer(PasswordEncoder passwordEncoder, TransactionTemplate transactionTemplate) {
        this.passwordEncoder = passwordEncoder;
        this.transactionTemplate = transactionTemplate;
    }

    @Bean
    public CommandLineRunner initData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            StudentRepository studentRepository,
            TrainerRepository trainerRepository,
            SpecialtyRepository specialtyRepository,
            GroupRepository groupRepository,
            CourseRepository courseRepository,
            SessionRepository sessionRepository) {
        return args -> {
            try {
                transactionTemplate.execute(status -> {
                    initializeTestData(
                        roleRepository,
                        userRepository,
                        studentRepository,
                        trainerRepository,
                        specialtyRepository,
                        groupRepository,
                        courseRepository,
                        sessionRepository
                    );
                    return null;
                });
            } catch (Exception e) {
                logger.warn("Could not initialize test data: {}", e.getMessage(), e);
            }
        };
    }

    private void initializeTestData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            StudentRepository studentRepository,
            TrainerRepository trainerRepository,
            SpecialtyRepository specialtyRepository,
            GroupRepository groupRepository,
            CourseRepository courseRepository,
            SessionRepository sessionRepository) {
        // Créer des données de test
        if (studentRepository.count() == 0) {
            // Spécialités - vérifier l'existence avant de créer
            Specialty info = specialtyRepository.findByNom("Informatique")
                .orElseGet(() -> {
                    Specialty s = new Specialty();
                    s.setNom("Informatique");
                    s.setDescription("Spécialité en informatique");
                    return specialtyRepository.save(s);
                });

            Specialty reseaux = specialtyRepository.findByNom("Réseaux")
                .orElseGet(() -> {
                    Specialty s = new Specialty();
                    s.setNom("Réseaux");
                    s.setDescription("Spécialité en réseaux");
                    return specialtyRepository.save(s);
                });

            // Groupes - vérifier l'existence avant de créer
            Group tp1 = groupRepository.findByNom("TP1")
                .orElseGet(() -> {
                    Group g = new Group();
                    g.setNom("TP1");
                    g.setDescription("Groupe TP1");
                    return groupRepository.save(g);
                });

            // Étudiant de test - vérifier l'existence avant de créer
            Student student = (Student) userRepository.findByUsername("etudiant1")
                .filter(u -> u instanceof Student)
                .map(u -> (Student) u)
                .orElseGet(() -> {
                    Student s = new Student();
                    s.setUsername("etudiant1");
                    s.setPassword(passwordEncoder.encode("password"));
                    s.setEmail("etudiant1@formation.com");
                    s.setNom("Dupont");
                    s.setPrenom("Jean");
                    s.setMatricule("ETU001");
                    s.setDateInscription(LocalDate.now());
                    s.setSpecialty(info);
                    s.setGroup(tp1);
                    
                    Set<Role> studentRoles = new HashSet<>();
                    studentRoles.add(roleRepository.findByName(Role.RoleType.ETUDIANT).orElseThrow());
                    s.setRoles(studentRoles);
                    
                    return studentRepository.save(s);
                });

            // Formateur de test - vérifier l'existence avant de créer
            Trainer trainer = (Trainer) userRepository.findByUsername("formateur1")
                .filter(u -> u instanceof Trainer)
                .map(u -> (Trainer) u)
                .orElseGet(() -> {
                    Trainer t = new Trainer();
                    t.setUsername("formateur1");
                    t.setPassword(passwordEncoder.encode("password"));
                    t.setEmail("formateur1@formation.com");
                    t.setNom("Martin");
                    t.setPrenom("Pierre");
                    t.setSpecialite("Java/Spring");
                    
                    Set<Role> trainerRoles = new HashSet<>();
                    trainerRoles.add(roleRepository.findByName(Role.RoleType.FORMATEUR).orElseThrow());
                    t.setRoles(trainerRoles);
                    
                    return trainerRepository.save(t);
                });

            // Session - vérifier l'existence avant de créer
            Session session = sessionRepository.findByNom("Session 2024-2025")
                .orElseGet(() -> {
                    Session s = new Session();
                    s.setNom("Session 2024-2025");
                    s.setDateDebut(LocalDate.of(2024, 9, 1));
                    s.setDateFin(LocalDate.of(2025, 6, 30));
                    s.setType(Session.SessionType.ANNEE_SCOLAIRE);
                    return sessionRepository.save(s);
                });

            // Cours - vérifier l'existence avant de créer
            Course course = courseRepository.findByCode("JEE001")
                .orElseGet(() -> {
                    Course c = new Course();
                    c.setCode("JEE001");
                    c.setTitre("Java Enterprise Edition");
                    c.setDescription("Introduction à JEE et Spring Boot");
                    c.setFormateur(trainer);
                    c.setSession(session);
                    return courseRepository.save(c);
                });
        }
    }
}


