package com.formation.controller.admin;

import com.formation.entity.Student;
import com.formation.service.StudentService;
import com.formation.service.SpecialtyService;
import com.formation.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/students")
public class AdminStudentController {
    
    private final StudentService studentService;
    private final SpecialtyService specialtyService;
    private final GroupService groupService;
    
    public AdminStudentController(StudentService studentService,
                                  SpecialtyService specialtyService,
                                  GroupService groupService) {
        this.studentService = studentService;
        this.specialtyService = specialtyService;
        this.groupService = groupService;
    }
    
    @GetMapping
    public String listStudents(Model model) {
        model.addAttribute("students", studentService.findAll());
        return "admin/students/list";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("specialties", specialtyService.findAll());
        model.addAttribute("groups", groupService.findAll());
        return "admin/students/form";
    }
    
    @PostMapping
    public String createStudent(@ModelAttribute Student student, RedirectAttributes redirectAttributes) {
        try {
            studentService.save(student);
            redirectAttributes.addFlashAttribute("success", "Étudiant créé avec succès");
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


