package com.formation.controller;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

/**
 * Classe de base pour les contrôleurs avec méthodes communes
 * Principe SOLID : DRY (Don't Repeat Yourself)
 */
public abstract class BaseController {
    
    /**
     * Ajoute un message d'erreur au modèle
     */
    protected void addError(Model model, String message) {
        model.addAttribute("error", message);
    }
    
    /**
     * Ajoute un message de succès au modèle
     */
    protected void addSuccess(Model model, String message) {
        model.addAttribute("success", message);
    }
    
    /**
     * Gère les exceptions et retourne la vue d'erreur appropriée
     */
    protected String handleException(Exception e, Model model, String errorView) {
        model.addAttribute("error", "Erreur: " + e.getMessage());
        return errorView;
    }
    
    /**
     * Récupère le nom d'utilisateur depuis l'authentification
     */
    protected String getUsername(Authentication authentication) {
        return authentication != null ? authentication.getName() : null;
    }
}

