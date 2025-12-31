package com.formation.controller.admin;

import com.formation.entity.Trainer;
import com.formation.service.TrainerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/trainers")
public class AdminTrainerController extends BaseAdminController<Trainer, Long> {
    
    private final TrainerService trainerService;
    
    public AdminTrainerController(TrainerService trainerService) {
        super("trainer", "trainers", "trainers");
        this.trainerService = trainerService;
    }
    
    @GetMapping
    public String listTrainers(Model model) {
        return list(model, trainerService.findAll());
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        return showCreateForm(model, Trainer::new);
    }
    
    @PostMapping
    public String createTrainer(@ModelAttribute Trainer trainer, RedirectAttributes redirectAttributes) {
        return handleCreate(() -> trainerService.save(trainer), redirectAttributes);
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


