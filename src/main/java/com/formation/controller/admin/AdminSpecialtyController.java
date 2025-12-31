package com.formation.controller.admin;

import com.formation.entity.Specialty;
import com.formation.service.SpecialtyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/specialties")
public class AdminSpecialtyController extends BaseAdminController<Specialty, Long> {
    
    private final SpecialtyService specialtyService;
    
    public AdminSpecialtyController(SpecialtyService specialtyService) {
        super("specialty", "specialties", "specialties");
        this.specialtyService = specialtyService;
    }
    
    @GetMapping
    public String listSpecialties(Model model) {
        return list(model, specialtyService.findAll());
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        return showCreateForm(model, Specialty::new);
    }
    
    @PostMapping
    public String createSpecialty(@ModelAttribute Specialty specialty, RedirectAttributes redirectAttributes) {
        return handleCreate(() -> specialtyService.save(specialty), redirectAttributes);
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        return showEditForm(id, model, specialtyService.findById(id));
    }
    
    @PostMapping("/{id}")
    public String updateSpecialty(@PathVariable Long id, @ModelAttribute Specialty specialty, 
                                 RedirectAttributes redirectAttributes) {
        return handleUpdate(() -> specialtyService.update(id, specialty), redirectAttributes);
    }
    
    @GetMapping("/{id}/delete")
    public String deleteSpecialty(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return handleDelete(() -> specialtyService.delete(id), redirectAttributes);
    }
}

