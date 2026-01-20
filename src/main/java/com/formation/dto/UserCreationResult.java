package com.formation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Résultat de la création d'un utilisateur par l'admin
 * Contient les informations nécessaires pour envoyer l'email avec les coordonnées
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationResult {
    private String username;
    private String password; // Mot de passe en clair (avant encodage)
    private String email;
    private String fullName; // Nom complet (nom + prénom)
    private String userType; // "ETUDIANT" ou "FORMATEUR"
}

