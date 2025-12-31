package com.formation.controller;

import com.formation.dto.RegistrationDto;
import com.formation.entity.Specialty;
import com.formation.repository.SpecialtyRepository;
import com.formation.repository.GroupRepository;
import com.formation.repository.TrainerRepository;
import com.formation.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class RegistrationController {
    
    private final RegistrationService registrationService;
    
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }
    
    @Autowired
    private SpecialtyRepository specialtyRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private TrainerRepository trainerRepository;
    
    /**
     * Récupère toutes les spécialités disponibles (de la table Specialty + spécialités des formateurs)
     * Crée automatiquement les spécialités des formateurs qui n'existent pas encore
     */
    private List<Specialty> getAllAvailableSpecialties() {
        // Récupérer toutes les spécialités existantes
        List<Specialty> allSpecialties = new ArrayList<>(specialtyRepository.findAll());
        
        // Récupérer les noms uniques des spécialités existantes
        Set<String> existingSpecialtyNames = allSpecialties.stream()
            .map(Specialty::getNom)
            .collect(Collectors.toSet());
        
        // Récupérer les spécialités distinctes des formateurs
        Set<String> trainerSpecialties = trainerRepository.findAll().stream()
            .map(t -> t.getSpecialite() != null ? t.getSpecialite().trim() : null)
            .filter(s -> s != null && !s.isEmpty())
            .collect(Collectors.toSet());
        
        // Créer automatiquement les spécialités des formateurs qui n'existent pas encore
        for (String trainerSpecialty : trainerSpecialties) {
            if (!existingSpecialtyNames.contains(trainerSpecialty)) {
                // Créer une nouvelle spécialité
                Specialty newSpecialty = new Specialty();
                newSpecialty.setNom(trainerSpecialty);
                newSpecialty.setDescription("Spécialité proposée par un formateur");
                newSpecialty = specialtyRepository.save(newSpecialty);
                allSpecialties.add(newSpecialty);
                existingSpecialtyNames.add(trainerSpecialty); // Ajouter au set pour éviter les doublons
            }
        }
        
        return allSpecialties;
    }
    
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationDto", new RegistrationDto());
        model.addAttribute("specialties", getAllAvailableSpecialties());
        model.addAttribute("groups", groupRepository.findAll());
        return "register";
    }
    
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationDto") RegistrationDto registrationDto,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("specialties", getAllAvailableSpecialties());
            model.addAttribute("groups", groupRepository.findAll());
            return "register";
        }
        
        try {
            registrationService.register(registrationDto);
            redirectAttributes.addFlashAttribute("success", 
                "Inscription réussie ! Vous pouvez maintenant vous connecter.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("specialties", getAllAvailableSpecialties());
            model.addAttribute("groups", groupRepository.findAll());
            return "register";
        }
    }
}

