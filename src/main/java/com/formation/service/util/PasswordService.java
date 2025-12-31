package com.formation.service.util;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Service utilitaire pour la gestion des mots de passe
 * Principe SOLID : Single Responsibility - une seule responsabilité
 */
@Component
public class PasswordService {
    
    private final PasswordEncoder passwordEncoder;
    private static final String DEFAULT_PASSWORD = "password";
    
    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Encode un mot de passe
     */
    public String encode(String password) {
        if (password == null || password.trim().isEmpty()) {
            return null;
        }
        return passwordEncoder.encode(password);
    }
    
    /**
     * Encode un mot de passe ou retourne un mot de passe par défaut si null/vide
     */
    public String encodeOrDefault(String password) {
        if (password == null || password.trim().isEmpty()) {
            return encode(DEFAULT_PASSWORD);
        }
        return encode(password);
    }
    
    /**
     * Vérifie si un mot de passe correspond au hash
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}

