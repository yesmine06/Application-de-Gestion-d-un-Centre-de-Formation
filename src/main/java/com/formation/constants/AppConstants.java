package com.formation.constants;

/**
 * Constantes de l'application
 * Principe : Centraliser les valeurs magiques
 */
public final class AppConstants {
    
    // Notes
    public static final double PASSING_GRADE = 10.0;
    
    // Génération de matricule
    public static final int MAX_MATRICULE_GENERATION_ATTEMPTS = 100;
    public static final String MATRICULE_PREFIX = "ETU";
    
    // Fichiers
    public static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
    public static final String UPLOAD_DIR = "uploads/courses/";
    
    // Groupes
    public static final String DEFAULT_GROUP_DESCRIPTION = "Groupe créé lors de l'inscription";
    public static final String DEFAULT_SPECIALTY_DESCRIPTION = "Spécialité créée automatiquement lors de l'inscription du formateur";
    public static final String DEFAULT_SPECIALTY_NAME = "Non spécifiée";
    
    private AppConstants() {
        // Classe utilitaire - pas d'instanciation
    }
}

