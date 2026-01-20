package com.formation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Contrôleur d'inscription - DÉSACTIVÉ
 * L'inscription publique est désactivée. Seul l'admin peut créer des comptes.
 * Les endpoints redirigent vers la page de connexion.
 */
@Controller
public class RegistrationController {
    
    /**
     * L'inscription publique est désactivée.
     * Redirige vers la page de connexion avec un message.
     */
    @GetMapping("/register")
    public String showRegistrationForm() {
        return "redirect:/login?registrationDisabled=true";
    }
    
    /**
     * L'inscription publique est désactivée.
     * Redirige vers la page de connexion avec un message.
     */
    @PostMapping("/register")
    public String register() {
        return "redirect:/login?registrationDisabled=true";
    }
}

