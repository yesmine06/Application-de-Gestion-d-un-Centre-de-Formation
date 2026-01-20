package com.formation.service.util;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Service utilitaire pour la gestion des mots de passe
 * Principe SOLID : Single Responsibility - une seule responsabilité
 */
@Component
public class PasswordService {
    
    private final PasswordEncoder passwordEncoder;
    private static final String DEFAULT_PASSWORD = "password";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String ALL_CHARS = UPPER + LOWER + DIGITS;
    private static final int PASSWORD_LENGTH = 12;
    private static final SecureRandom random = new SecureRandom();
    
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
     * Génère un mot de passe aléatoire sécurisé
     */
    public String generateRandomPassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        
        // Assurer au moins un caractère de chaque type
        password.append(UPPER.charAt(random.nextInt(UPPER.length())));
        password.append(LOWER.charAt(random.nextInt(LOWER.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        
        // Remplir le reste avec des caractères aléatoires
        for (int i = 3; i < PASSWORD_LENGTH; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }
        
        // Mélanger les caractères
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }
    
    /**
     * Vérifie si un mot de passe correspond au hash
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}

