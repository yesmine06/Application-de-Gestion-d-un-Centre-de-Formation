package com.formation;

import com.formation.entity.*;
import com.formation.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class GestionFormationApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(GestionFormationApplication.class);

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private TrainerRepository trainerRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(GestionFormationApplication.class, args);
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Créer les rôles
        try {
            if (roleRepository.count() == 0) {
                Role adminRole = new Role();
                adminRole.setName(Role.RoleType.ADMIN);
                roleRepository.save(adminRole);

                Role formateurRole = new Role();
                formateurRole.setName(Role.RoleType.FORMATEUR);
                roleRepository.save(formateurRole);

                Role etudiantRole = new Role();
                etudiantRole.setName(Role.RoleType.ETUDIANT);
                roleRepository.save(etudiantRole);
            }

            // Créer ou mettre à jour un utilisateur admin par défaut
            User admin = userRepository.findByUsername("admin").orElse(null);
            if (admin == null) {
                // Créer un nouvel admin
                admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setEmail("admin@formation.com");
                admin.setNom("Admin");
                admin.setPrenom("System");
                admin.setEnabled(true);
                
                Set<Role> roles = new HashSet<>();
                roles.add(roleRepository.findByName(Role.RoleType.ADMIN).orElseThrow());
                admin.setRoles(roles);
                
                admin = userRepository.save(admin);
                logger.info("✓ Admin créé avec succès");
            } else {
                // Réinitialiser le mot de passe de l'admin existant pour garantir qu'il fonctionne
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setEnabled(true);
                admin = userRepository.save(admin);
                logger.info("✓ Mot de passe admin réinitialisé");
            }
        } catch (Exception e) {
            // Ignore if tables don't exist yet (will be created by Hibernate)
            logger.warn("Could not initialize default data: {}", e.getMessage(), e);
        }
    }
}


