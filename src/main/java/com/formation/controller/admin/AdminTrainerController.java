package com.formation.controller.admin;

import com.formation.constants.UserType;
import com.formation.dto.RegistrationDto;
import com.formation.dto.UserCreationResult;
import com.formation.entity.Trainer;
import com.formation.service.EmailService;
import com.formation.service.RegistrationService;
import com.formation.service.TrainerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/trainers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTrainerController extends BaseAdminController<Trainer, Long> {
    
    private final TrainerService trainerService;
    private final RegistrationService registrationService;
    private final EmailService emailService;
    
    public AdminTrainerController(TrainerService trainerService,
                                  RegistrationService registrationService,
                                  EmailService emailService) {
        super("trainer", "trainers", "trainers");
        this.trainerService = trainerService;
        this.registrationService = registrationService;
        this.emailService = emailService;
    }
    
    @GetMapping
    public String listTrainers(Model model) {
        return list(model, trainerService.findAll());
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setUserType(UserType.FORMATEUR.toString());
        model.addAttribute("registrationDto", registrationDto);
        return "admin/trainers/form";
    }
    
    @PostMapping
    public String createTrainer(@ModelAttribute RegistrationDto registrationDto, RedirectAttributes redirectAttributes) {
        try {
            // S'assurer que le type est FORMATEUR
            registrationDto.setUserType(UserType.FORMATEUR.toString());
            
            // Log pour débogage
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AdminTrainerController.class);
            logger.info("Création d'un formateur - Username: {}, Email: {}, Spécialité: {}", 
                registrationDto.getUsername(), registrationDto.getEmail(), registrationDto.getSpecialite());
            
            // Créer l'utilisateur via RegistrationService (génère le mot de passe automatiquement)
            UserCreationResult result = registrationService.createUserByAdmin(registrationDto);
            
            if (result == null) {
                throw new RuntimeException("La création du formateur a échoué - résultat null");
            }
            
            logger.info("Formateur créé avec succès - ID: {}, Username: {}", result.getUsername(), result.getUsername());
            
            // Envoyer l'email avec les coordonnées
            emailService.sendAccountCredentials(
                result.getEmail(),
                result.getFullName(),
                result.getUsername(),
                result.getPassword(),
                result.getUserType()
            );
            
            redirectAttributes.addFlashAttribute("success", 
                "Formateur créé avec succès. Les coordonnées de connexion ont été envoyées par email.");
        } catch (Exception e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AdminTrainerController.class);
            logger.error("Erreur lors de la création du formateur", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création: " + e.getMessage());
        }
        return "redirect:/admin/trainers";
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        return showEditForm(id, model, trainerService.findById(id));
    }
    
    @PostMapping("/{id}")
    public String updateTrainer(@PathVariable Long id, @ModelAttribute Trainer trainer, 
                               RedirectAttributes redirectAttributes) {
        return handleUpdate(() -> trainerService.update(id, trainer), redirectAttributes);
    }
    
    @GetMapping("/{id}/delete")
    public String deleteTrainer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return handleDelete(() -> trainerService.delete(id), redirectAttributes);
    }
}


