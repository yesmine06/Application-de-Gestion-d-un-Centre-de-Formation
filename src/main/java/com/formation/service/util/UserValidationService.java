package com.formation.service.util;

import com.formation.entity.User;
import com.formation.exception.ValidationException;
import com.formation.repository.UserRepository;
import org.springframework.stereotype.Component;

/**
 * Service de validation pour les utilisateurs
 * Principe SOLID : Single Responsibility
 */
@Component
public class UserValidationService {
    
    private final UserRepository userRepository;
    
    public UserValidationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Valide l'unicité du username
     */
    public void validateUsernameUnique(String username, Long excludeId) {
        if (username == null || username.trim().isEmpty()) {
            return; // Sera géré ailleurs
        }
        
        userRepository.findByUsername(username).ifPresent(user -> {
            if (excludeId == null || !user.getId().equals(excludeId)) {
                throw new ValidationException("Ce nom d'utilisateur est déjà utilisé: " + username);
            }
        });
    }
    
    /**
     * Valide l'unicité de l'email
     */
    public void validateEmailUnique(String email, Long excludeId) {
        if (email == null || email.trim().isEmpty()) {
            return;
        }
        
        userRepository.findByEmail(email).ifPresent(user -> {
            if (excludeId == null || !user.getId().equals(excludeId)) {
                throw new ValidationException("Cet email est déjà utilisé: " + email);
            }
        });
    }
    
    /**
     * Valide username et email
     */
    public void validateUserUniqueness(User user) {
        validateUsernameUnique(user.getUsername(), user.getId());
        validateEmailUnique(user.getEmail(), user.getId());
    }
}

