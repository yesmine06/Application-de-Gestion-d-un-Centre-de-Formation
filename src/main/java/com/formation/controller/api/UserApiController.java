package com.formation.controller.api;

import com.formation.entity.User;
import com.formation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserApiController {
    
    private final UserRepository userRepository;
    
    public UserApiController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElse(null);
        
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("nom", user.getNom());
        userInfo.put("prenom", user.getPrenom());
        userInfo.put("roles", user.getRoles().stream()
            .map(role -> role.getName().name())
            .toList());
        
        // Add type-specific information
        if (user instanceof com.formation.entity.Student) {
            com.formation.entity.Student student = (com.formation.entity.Student) user;
            userInfo.put("type", "STUDENT");
            userInfo.put("matricule", student.getMatricule());
            userInfo.put("studentId", student.getId());
        } else if (user instanceof com.formation.entity.Trainer) {
            com.formation.entity.Trainer trainer = (com.formation.entity.Trainer) user;
            userInfo.put("type", "TRAINER");
            userInfo.put("specialite", trainer.getSpecialite());
            userInfo.put("trainerId", trainer.getId());
        } else {
            userInfo.put("type", "USER");
        }
        
        return ResponseEntity.ok(userInfo);
    }
}

