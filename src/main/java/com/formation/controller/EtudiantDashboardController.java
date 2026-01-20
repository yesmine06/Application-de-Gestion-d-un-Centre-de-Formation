package com.formation.controller;

import com.formation.entity.Grade;
import com.formation.entity.Student;
import com.formation.service.EtudiantService;
import com.formation.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/etudiant")
@PreAuthorize("hasAnyRole('ETUDIANT', 'ADMIN', 'FORMATEUR')")
public class EtudiantDashboardController {
    
    private final EtudiantService etudiantService;
    
    public EtudiantDashboardController(EtudiantService etudiantService) {
        this.etudiantService = etudiantService;
    }
    
    @GetMapping("/dashboard")
    public String etudiantDashboard() {
        // Rediriger vers l'interface React
        return "redirect:/react/etudiant/index.html";
    }
}

