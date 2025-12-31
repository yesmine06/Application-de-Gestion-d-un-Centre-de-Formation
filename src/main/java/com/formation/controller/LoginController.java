package com.formation.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;
import java.util.stream.Collectors;

@Controller
public class LoginController {
    
    @GetMapping("/login")
    public String login(Model model, String error, String logout, Authentication authentication) {
        if (error != null) {
            if ("role".equals(error)) {
                model.addAttribute("error", "Vous n'avez pas le rôle sélectionné. Veuillez choisir un autre rôle.");
            } else {
                model.addAttribute("error", "Nom d'utilisateur ou mot de passe incorrect");
            }
        }
        if (logout != null) {
            model.addAttribute("message", "Vous avez été déconnecté avec succès");
        }
        
        // Afficher le rôle si l'utilisateur est déjà connecté
        if (authentication != null && authentication.isAuthenticated()) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            String roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.replace("ROLE_", ""))
                .collect(Collectors.joining(", "));
            model.addAttribute("userRole", roles);
            model.addAttribute("username", authentication.getName());
        }
        
        return "login";
    }
    
    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // Rediriger selon le rôle
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            if (!authorities.isEmpty()) {
                String firstRole = authorities.iterator().next().getAuthority().replace("ROLE_", "");
                return switch (firstRole) {
                    case "ADMIN" -> "redirect:/admin/dashboard";
                    case "FORMATEUR" -> "redirect:/formateur/dashboard";
                    case "ETUDIANT" -> "redirect:/etudiant/dashboard";
                    default -> "redirect:/dashboard";
                };
            }
        }
        return "redirect:/dashboard";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // Rediriger selon le rôle
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            if (!authorities.isEmpty()) {
                String firstRole = authorities.iterator().next().getAuthority().replace("ROLE_", "");
                return switch (firstRole) {
                    case "ADMIN" -> "redirect:/admin/dashboard";
                    case "FORMATEUR" -> "redirect:/formateur/dashboard";
                    case "ETUDIANT" -> "redirect:/etudiant/dashboard";
                    default -> "dashboard";
                };
            }
        }
        return "dashboard";
    }
}


