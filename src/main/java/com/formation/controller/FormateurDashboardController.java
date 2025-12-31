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
    public String dashboard(Model model, Authentication authentication) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(authentication.getName());
            long coursesCount = formateurService.getTrainerCourses(trainer.getId()).size();
            long studentsCount = formateurService.getTrainerStudents(trainer.getId()).size();
            long gradesCount = formateurService.getTrainerGrades(trainer.getId()).size();
            
            model.addAttribute("trainer", trainer);
            model.addAttribute("coursesCount", coursesCount);
            model.addAttribute("studentsCount", studentsCount);
            model.addAttribute("gradesCount", gradesCount);
            
            return "formateur/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
            return "formateur/dashboard";
        }
    }
}
