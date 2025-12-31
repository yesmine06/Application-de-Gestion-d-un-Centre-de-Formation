package com.formation.constants;

/**
 * Constantes pour les types d'utilisateurs
 * Principe : Éviter les magic strings
 */
public final class UserType {
    
    public static final String ETUDIANT = "ETUDIANT";
    public static final String FORMATEUR = "FORMATEUR";
    public static final String ADMIN = "ADMIN";
    
    private UserType() {
        // Classe utilitaire - pas d'instanciation
    }
}

