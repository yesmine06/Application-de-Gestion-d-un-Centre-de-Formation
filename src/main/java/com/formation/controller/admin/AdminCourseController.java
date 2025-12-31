package com.formation.controller.admin;

import com.formation.entity.Course;
import com.formation.service.CourseService;
import com.formation.service.TrainerService;
import com.formation.service.SessionService;
import com.formation.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/courses")
public class AdminCourseController {
    
    private final CourseService courseService;
    private final TrainerService trainerService;
    private final SessionService sessionService;
    private final GroupService groupService;
    
    public AdminCourseController(CourseService courseService,
                                TrainerService trainerService,
                                SessionService sessionService,
                                GroupService groupService) {
        this.courseService = courseService;
        this.trainerService = trainerService;
        this.sessionService = sessionService;
        this.groupService = groupService;
    }
    
    @GetMapping
    public String listCourses(Model model) {
        model.addAttribute("courses", courseService.findAll());
        return "admin/courses/list";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("trainers", trainerService.findAll());
        model.addAttribute("sessions", sessionService.findAll());
        model.addAttribute("groups", groupService.findAll());
        return "admin/courses/form";
    }
    
    @PostMapping
    public String createCourse(@ModelAttribute Course course,
                              @RequestParam(required = false) Long formateurId,
                              @RequestParam(required = false) Long sessionId,
                              RedirectAttributes redirectAttributes) {
        try {
            // Convertir les IDs en objets
            if (formateurId != null) {
                com.formation.entity.Trainer trainer = new com.formation.entity.Trainer();
                trainer.setId(formateurId);
                course.setFormateur(trainer);
            }
            if (sessionId != null) {
                com.formation.entity.Session session = new com.formation.entity.Session();
                session.setId(sessionId);
                course.setSession(session);
            }
            courseService.save(course);
            redirectAttributes.addFlashAttribute("success", "Cours créé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création: " + e.getMessage());
        }
        return "redirect:/admin/courses";
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        courseService.findById(id).ifPresent(course -> {
            model.addAttribute("course", course);
            model.addAttribute("trainers", trainerService.findAll());
            model.addAttribute("sessions", sessionService.findAll());
            model.addAttribute("groups", groupService.findAll());
        });
        return "admin/courses/form";
    }
    
    @PostMapping("/{id}")
    public String updateCourse(@PathVariable Long id, 
                              @ModelAttribute Course course,
                              @RequestParam(required = false) Long formateurId,
                              @RequestParam(required = false) Long sessionId,
                              RedirectAttributes redirectAttributes) {
        try {
            // Convertir les IDs en objets
            if (formateurId != null) {
                com.formation.entity.Trainer trainer = new com.formation.entity.Trainer();
                trainer.setId(formateurId);
                course.setFormateur(trainer);
            } else {
                course.setFormateur(null);
            }
            if (sessionId != null) {
                com.formation.entity.Session session = new com.formation.entity.Session();
                session.setId(sessionId);
                course.setSession(session);
            } else {
                course.setSession(null);
            }
            courseService.update(id, course);
            redirectAttributes.addFlashAttribute("success", "Cours mis à jour avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
        }
        return "redirect:/admin/courses";
    }
    
    @GetMapping("/{id}/delete")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            courseService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Cours supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/admin/courses";
    }
}


