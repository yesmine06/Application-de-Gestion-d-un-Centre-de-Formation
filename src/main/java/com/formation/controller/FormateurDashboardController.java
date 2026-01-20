package com.formation.controller;

import com.formation.entity.Trainer;
import com.formation.service.FormateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/formateur")
@PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN')")
public class FormateurDashboardController {
    
    private final FormateurService formateurService;
    
    public FormateurDashboardController(FormateurService formateurService) {
        this.formateurService = formateurService;
    }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        // Rediriger vers l'interface React
        return "redirect:/react/formateur/index.html";
    }
}
