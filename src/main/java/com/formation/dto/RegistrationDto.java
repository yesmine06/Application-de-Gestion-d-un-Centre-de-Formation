package com.formation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationDto {
    
    @NotBlank(message = "Le nom d'utilisateur est requis")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    private String username;
    
    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;
    
    @NotBlank(message = "La confirmation du mot de passe est requise")
    private String confirmPassword;
    
    @NotBlank(message = "L'email est requis")
    @Email(message = "L'email doit être valide")
    private String email;
    
    @NotBlank(message = "Le nom est requis")
    private String nom;
    
    @NotBlank(message = "Le prénom est requis")
    private String prenom;
    
    @NotBlank(message = "Le type d'utilisateur est requis")
    private String userType; // "ETUDIANT" ou "FORMATEUR"
    
    // Champs spécifiques pour étudiant
    private String matricule;
    private Long specialtyId;
    private Long groupId;
    private String groupName; // Nom du nouveau groupe à créer (si groupId est null)
    
    // Champs spécifiques pour formateur
    private String specialite;
}

