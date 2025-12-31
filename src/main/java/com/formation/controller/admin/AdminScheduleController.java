package com.formation.controller.admin;

import com.formation.entity.Schedule;
import com.formation.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/schedules")
public class AdminScheduleController {
    
    private final ScheduleService scheduleService;
    
    public AdminScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }
    
    /**
     * Liste toutes les séances avec leur statut
     */
    @GetMapping
    public String listSchedules(Model model) {
        List<Schedule> allSchedules = scheduleService.findAll();
        List<Schedule> pendingSchedules = scheduleService.findPendingSchedules();
        List<Schedule> conflicts = scheduleService.findAllConflicts();
        
        model.addAttribute("schedules", allSchedules);
        model.addAttribute("pendingSchedules", pendingSchedules);
        model.addAttribute("conflicts", conflicts);
        return "admin/schedules/list";
    }
    
    /**
     * Affiche les détails d'une séance avec possibilité d'édition
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Schedule schedule = scheduleService.findById(id)
            .orElseThrow(() -> new RuntimeException("Séance non trouvée"));
        model.addAttribute("schedule", schedule);
        model.addAttribute("course", schedule.getCours());
        return "admin/schedules/form";
    }
    
    /**
     * Met à jour une séance
     */
    @PostMapping("/{id}")
    public String updateSchedule(@PathVariable Long id,
                                @ModelAttribute Schedule scheduleDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            scheduleService.update(id, scheduleDetails);
            redirectAttributes.addFlashAttribute("success", "Séance modifiée avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la modification: " + e.getMessage());
        }
        return "redirect:/admin/schedules";
    }
    
    /**
     * Approuve une séance
     */
    @PostMapping("/{id}/approve")
    public String approveSchedule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            scheduleService.approve(id);
            redirectAttributes.addFlashAttribute("success", "Séance approuvée avec succès. Les étudiants ont été notifiés.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de l'approbation: " + e.getMessage());
        }
        return "redirect:/admin/schedules";
    }
    
    /**
     * Rejette une séance
     */
    @PostMapping("/{id}/reject")
    public String rejectSchedule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            scheduleService.reject(id);
            redirectAttributes.addFlashAttribute("success", "Séance rejetée");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors du rejet: " + e.getMessage());
        }
        return "redirect:/admin/schedules";
    }
    
    /**
     * Supprime une séance
     */
    @PostMapping("/{id}/delete")
    public String deleteSchedule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            scheduleService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Séance supprimée avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/admin/schedules";
    }
}

