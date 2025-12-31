package com.formation.service;

import com.formation.dto.PasswordChangeDto;
import com.formation.entity.User;
import com.formation.exception.BusinessException;
import com.formation.exception.ResourceNotFoundException;
import com.formation.exception.ValidationException;
import com.formation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserProfileService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Met à jour le profil de l'utilisateur connecté
     */
    public User updateProfile(String username, String nom, String prenom, String email) {
        User user = userRepository.findByUsernameWithRoles(username)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé: " + username));
        
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        
        return userRepository.save(user);
    }
    
    /**
     * Change le mot de passe de l'utilisateur connecté
     */
    public void changePassword(String username, PasswordChangeDto passwordDto) {
        User user = userRepository.findByUsernameWithRoles(username)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé: " + username));
        
        // Vérifier que le mot de passe actuel est correct
        if (!passwordEncoder.matches(passwordDto.getCurrentPassword(), user.getPassword())) {
            throw new ValidationException("Le mot de passe actuel est incorrect");
        }
        
        // Vérifier que les nouveaux mots de passe correspondent
        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
            throw new ValidationException("Les nouveaux mots de passe ne correspondent pas");
        }
        
        // Changer le mot de passe
        user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        userRepository.save(user);
    }
    
    /**
     * Récupère l'utilisateur connecté
     */
    public User getCurrentUser(String username) {
        return userRepository.findByUsernameWithRoles(username)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé: " + username));
    }
}

