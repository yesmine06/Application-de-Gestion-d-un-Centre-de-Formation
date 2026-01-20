package com.formation.controller.admin;

import com.formation.constants.UserType;
import com.formation.dto.RegistrationDto;
import com.formation.dto.UserCreationResult;
import com.formation.entity.Student;
import com.formation.service.EmailService;
import com.formation.service.RegistrationService;
import com.formation.service.StudentService;
import com.formation.service.SpecialtyService;
import com.formation.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/students")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStudentController {
    
    private final StudentService studentService;
    private final SpecialtyService specialtyService;
    private final GroupService groupService;
    private final RegistrationService registrationService;
    private final EmailService emailService;
    
    public AdminStudentController(StudentService studentService,
                                  SpecialtyService specialtyService,
                                  GroupService groupService,
                                  RegistrationService registrationService,
                                  EmailService emailService) {
        this.studentService = studentService;
        this.specialtyService = specialtyService;
        this.groupService = groupService;
        this.registrationService = registrationService;
        this.emailService = emailService;
    }
    
    @GetMapping
    public String listStudents(Model model) {
        model.addAttribute("students", studentService.findAll());
        return "admin/students/list";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setUserType(UserType.ETUDIANT.toString());
        model.addAttribute("registrationDto", registrationDto);
        model.addAttribute("specialties", specialtyService.findAll());
        model.addAttribute("groups", groupService.findAll());
        return "admin/students/form";
    }
    
    @PostMapping
    public String createStudent(@ModelAttribute RegistrationDto registrationDto, RedirectAttributes redirectAttributes) {
        try {
            // S'assurer que le type est ETUDIANT
            registrationDto.setUserType(UserType.ETUDIANT.toString());
            
            // Créer l'utilisateur via RegistrationService (génère le mot de passe automatiquement)
            UserCreationResult result = registrationService.createUserByAdmin(registrationDto);
            
            // Envoyer l'email avec les coordonnées
            emailService.sendAccountCredentials(
                result.getEmail(),
                result.getFullName(),
                result.getUsername(),
                result.getPassword(),
                result.getUserType()
            );
            
            redirectAttributes.addFlashAttribute("success", 
                "Étudiant créé avec succès. Les coordonnées de connexion ont été envoyées par email.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création: " + e.getMessage());
        }
        return "redirect:/admin/students";
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        studentService.findById(id).ifPresent(student -> {
            model.addAttribute("student", student);
            model.addAttribute("specialties", specialtyService.findAll());
            model.addAttribute("groups", groupService.findAll());
        });
        return "admin/students/form";
    }
    
    @PostMapping("/{id}")
    public String updateStudent(@PathVariable Long id, @ModelAttribute Student student, 
                               RedirectAttributes redirectAttributes) {
        try {
            studentService.update(id, student);
            redirectAttributes.addFlashAttribute("success", "Étudiant mis à jour avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
        }
        return "redirect:/admin/students";
    }
    
    @GetMapping("/{id}/delete")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            studentService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Étudiant supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/admin/students";
    }
}


