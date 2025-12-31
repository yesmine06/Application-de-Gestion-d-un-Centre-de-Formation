package com.formation.controller;

import com.formation.dto.PasswordChangeDto;
import com.formation.entity.User;
import com.formation.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@PreAuthorize("isAuthenticated()")
public class ProfileController {
    
    private final UserProfileService userProfileService;
    
    public ProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }
    
    /**
     * Affiche le profil de l'utilisateur connecté
     */
    @GetMapping
    public String showProfile(Model model, Authentication authentication) {
        User user = userProfileService.getCurrentUser(authentication.getName());
        model.addAttribute("user", user);
        model.addAttribute("passwordChangeDto", new PasswordChangeDto());
        return "profile";
    }
    
    /**
     * Met à jour le profil
     */
    @PostMapping("/update")
    public String updateProfile(@RequestParam String nom,
                               @RequestParam String prenom,
                               @RequestParam String email,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            userProfileService.updateProfile(authentication.getName(), nom, prenom, email);
            redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/profile";
    }
    
    /**
     * Change le mot de passe
     */
    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute PasswordChangeDto passwordChangeDto,
                                BindingResult result,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            User user = userProfileService.getCurrentUser(authentication.getName());
            model.addAttribute("user", user);
            return "profile";
        }
        
        try {
            userProfileService.changePassword(authentication.getName(), passwordChangeDto);
            redirectAttributes.addFlashAttribute("success", "Mot de passe changé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/profile";
    }
}

