package com.formation.controller;

import com.formation.entity.*;
import com.formation.exception.BusinessException;
import com.formation.exception.ResourceNotFoundException;
import com.formation.service.FormateurService;
import com.formation.service.GradeService;
import com.formation.service.CourseFileService;
import com.formation.service.CourseService;
import com.formation.service.SessionService;
import com.formation.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Controller
@RequestMapping("/formateur")
@PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN')")
public class FormateurController extends BaseController {
    
    private final FormateurService formateurService;
    private final GradeService gradeService;
    private final CourseFileService courseFileService;
    private final CourseService courseService;
    private final SessionService sessionService;
    private final ScheduleService scheduleService;
    
    @Autowired
    public FormateurController(FormateurService formateurService,
                              GradeService gradeService,
                              CourseFileService courseFileService,
                              CourseService courseService,
                              SessionService sessionService,
                              ScheduleService scheduleService) {
        this.formateurService = formateurService;
        this.gradeService = gradeService;
        this.courseFileService = courseFileService;
        this.courseService = courseService;
        this.sessionService = sessionService;
        this.scheduleService = scheduleService;
    }
    
    /**
     * Affiche les cours du formateur
     */
    @GetMapping("/courses")
    public String myCourses(Model model, Authentication authentication) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            List<Course> courses = formateurService.getTrainerCourses(trainer.getId());
            model.addAttribute("courses", courses);
            model.addAttribute("trainer", trainer);
            return "formateur/courses";
        } catch (Exception e) {
            return handleException(e, model, "formateur/dashboard");
        }
    }
    
    /**
     * Affiche les détails d'un cours avec ses étudiants
     */
    @GetMapping("/courses/{courseId}")
    public String courseDetails(@PathVariable Long courseId, Model model, Authentication authentication) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            Course course = formateurService.getTrainerCourse(trainer.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé"));
            
            List<Student> students = formateurService.getStudentsByCourse(trainer.getId(), courseId);
            List<Grade> grades = formateurService.getGradesByCourse(trainer.getId(), courseId);
            
            // Créer une Map pour faciliter l'accès aux notes par étudiant
            java.util.Map<Long, Grade> gradeMap = grades.stream()
                .collect(java.util.stream.Collectors.toMap(
                    g -> g.getStudent().getId(),
                    g -> g,
                    (existing, replacement) -> existing
                ));
            
            // Calculer la moyenne
            Double average = GradeService.calculateAverage(grades);
            
            model.addAttribute("course", course);
            model.addAttribute("students", students);
            model.addAttribute("grades", grades);
            model.addAttribute("gradeMap", gradeMap);
            model.addAttribute("trainer", trainer);
            model.addAttribute("average", average);
            
            return "formateur/course-details";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/formateur/courses";
        }
    }
    
    /**
     * Affiche tous les étudiants du formateur
     */
    @GetMapping("/students")
    public String myStudents(Model model, Authentication authentication) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            List<Student> students = formateurService.getTrainerStudents(trainer.getId());
            model.addAttribute("students", students);
            model.addAttribute("trainer", trainer);
            return "formateur/students";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
            return "formateur/dashboard";
        }
    }
    
    /**
     * Affiche toutes les notes des étudiants du formateur
     */
    @GetMapping("/grades")
    public String myGrades(@RequestParam(required = false) Long courseId,
                          Model model, Authentication authentication) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            List<Grade> grades;
            
            if (courseId != null) {
                grades = formateurService.getGradesByCourse(trainer.getId(), courseId);
            } else {
                grades = formateurService.getTrainerGrades(trainer.getId());
            }
            
            List<Course> courses = formateurService.getTrainerCourses(trainer.getId());
            
            model.addAttribute("grades", grades);
            model.addAttribute("courses", courses);
            model.addAttribute("trainer", trainer);
            model.addAttribute("selectedCourseId", courseId);
            return "formateur/grades";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
            return "formateur/dashboard";
        }
    }
    
    /**
     * Affiche le formulaire pour attribuer/modifier une note
     */
    @GetMapping("/grades/new")
    public String showGradeForm(@RequestParam(required = false) Long courseId,
                                @RequestParam(required = false) Long studentId,
                                Model model, Authentication authentication) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            List<Course> courses = formateurService.getTrainerCourses(trainer.getId());
            
            model.addAttribute("courses", courses);
            model.addAttribute("courseId", courseId);
            model.addAttribute("studentId", studentId);
            
            if (courseId != null) {
                Course course = formateurService.getTrainerCourse(trainer.getId(), courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé"));
                List<Student> students = formateurService.getStudentsByCourse(trainer.getId(), courseId);
                model.addAttribute("course", course);
                model.addAttribute("students", students);
            }
            
            return "formateur/grade-form";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/formateur/grades";
        }
    }
    
    /**
     * Traite le formulaire pour attribuer/modifier une note
     */
    @PostMapping("/grades/save")
    public String saveGrade(@RequestParam Long courseId,
                           @RequestParam Long studentId,
                           @RequestParam Double valeur,
                           @RequestParam(required = false) String commentaire,
                           RedirectAttributes redirectAttributes,
                           Authentication authentication) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            
            // Vérifier que le cours appartient au formateur
            Course course = formateurService.getTrainerCourse(trainer.getId(), courseId)
                .orElseThrow(() -> new BusinessException("Cours non trouvé ou ne vous appartient pas"));
            
            // Utiliser le GradeService pour sauvegarder
            gradeService.saveOrUpdate(studentId, courseId, valeur, commentaire);
            
            redirectAttributes.addFlashAttribute("success", "Note attribuée avec succès");
            return "redirect:/formateur/courses/" + courseId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/formateur/grades/new?courseId=" + courseId + "&studentId=" + studentId;
        }
    }
    
    /**
     * Affiche la page de gestion des fichiers d'un cours
     */
    @GetMapping("/courses/{courseId}/files")
    public String courseFiles(@PathVariable Long courseId, Model model, Authentication authentication) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            Course course = formateurService.getTrainerCourse(trainer.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé"));
            
            List<CourseFile> files = courseFileService.getFilesByCourse(courseId);
            
            model.addAttribute("course", course);
            model.addAttribute("files", files);
            model.addAttribute("trainer", trainer);
            
            return "formateur/course-files";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/formateur/courses";
        }
    }
    
    /**
     * Upload un fichier pour un cours
     */
    @PostMapping("/courses/{courseId}/files/upload")
    public String uploadFile(@PathVariable Long courseId,
                            @RequestParam("file") MultipartFile file,
                            @RequestParam(required = false) String description,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            Course course = formateurService.getTrainerCourse(trainer.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé"));
            
            courseFileService.uploadFile(courseId, file, description);
            redirectAttributes.addFlashAttribute("success", "Fichier uploadé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'upload: " + e.getMessage());
        }
        return "redirect:/formateur/courses/" + courseId + "/files";
    }
    
    /**
     * Télécharge un fichier
     */
    @GetMapping("/courses/{courseId}/files/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long courseId,
                                                 @PathVariable Long fileId,
                                                 Authentication authentication) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            Course course = formateurService.getTrainerCourse(trainer.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé"));
            
            CourseFile file = courseFileService.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Fichier non trouvé"));
            
            Path filePath = courseFileService.getFilePath(fileId);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.getContentType() != null ? file.getContentType() : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getOriginalFileName() + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Supprime un fichier
     */
    @PostMapping("/courses/{courseId}/files/{fileId}/delete")
    public String deleteFile(@PathVariable Long courseId,
                            @PathVariable Long fileId,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            Course course = formateurService.getTrainerCourse(trainer.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé"));
            
            courseFileService.deleteFile(fileId);
            redirectAttributes.addFlashAttribute("success", "Fichier supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/formateur/courses/" + courseId + "/files";
    }
    
    /**
     * Affiche le formulaire de création de cours
     */
    @GetMapping("/courses/new")
    public String showCreateCourseForm(Model model, Authentication authentication) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            model.addAttribute("course", new Course());
            model.addAttribute("sessions", sessionService.findAll());
            model.addAttribute("trainer", trainer);
            return "formateur/course-form";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/formateur/courses";
        }
    }
    
    /**
     * Crée un nouveau cours (le formateur connecté est automatiquement assigné)
     */
    @PostMapping("/courses")
    public String createCourse(@ModelAttribute Course course,
                              @RequestParam(required = false) Long sessionId,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            course.setFormateur(trainer);
            
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
        return "redirect:/formateur/courses";
    }
    
    /**
     * Affiche tous les groupes avec leurs étudiants
     */
    @GetMapping("/groups")
    public String viewGroups(Model model, Authentication authentication) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            List<Group> groups = formateurService.getAllGroups();
            
            // Créer une map pour chaque groupe avec ses étudiants
            java.util.Map<Long, List<Student>> groupStudentsMap = new java.util.HashMap<>();
            for (Group group : groups) {
                List<Student> students = formateurService.getStudentsByGroup(group.getId());
                groupStudentsMap.put(group.getId(), students);
            }
            
            model.addAttribute("groups", groups);
            model.addAttribute("groupStudentsMap", groupStudentsMap);
            model.addAttribute("trainer", trainer);
            return "formateur/groups";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
            return "formateur/dashboard";
        }
    }
    
    /**
     * Affiche les séances planifiées pour les cours du formateur
     */
    @GetMapping("/schedules")
    public String mySchedules(Model model, Authentication authentication) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            List<Schedule> schedules = scheduleService.findByTrainer(trainer.getId());
            model.addAttribute("schedules", schedules);
            model.addAttribute("trainer", trainer);
            return "formateur/schedules";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
            return "formateur/dashboard";
        }
    }
    
    /**
     * Affiche le formulaire de création de séance pour un cours
     */
    @GetMapping("/courses/{courseId}/schedules/new")
    public String showCreateScheduleForm(@PathVariable Long courseId, Model model, Authentication authentication) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            Course course = formateurService.getTrainerCourse(trainer.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé"));
            
            Schedule schedule = new Schedule();
            schedule.setCours(course);
            model.addAttribute("schedule", schedule);
            model.addAttribute("course", course);
            return "formateur/schedule-form";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/formateur/courses";
        }
    }
    
    /**
     * Crée une nouvelle séance
     */
    @PostMapping("/courses/{courseId}/schedules")
    public String createSchedule(@PathVariable Long courseId,
                                @ModelAttribute Schedule schedule,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            Course course = formateurService.getTrainerCourse(trainer.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé"));
            
            schedule.setCours(course);
            // Laisser le statut PENDING par défaut pour validation par l'admin
            // Le statut sera défini automatiquement à PENDING dans ScheduleService si null
            
            scheduleService.save(schedule);
            redirectAttributes.addFlashAttribute("success", 
                "Séance planifiée avec succès. Elle est en attente de validation par l'administrateur.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la planification: " + e.getMessage());
        }
        return "redirect:/formateur/courses/" + courseId;
    }
    
    /**
     * Affiche le formulaire d'édition d'une séance
     */
    @GetMapping("/schedules/{scheduleId}/edit")
    public String showEditScheduleForm(@PathVariable Long scheduleId, Model model, Authentication authentication) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            Schedule schedule = scheduleService.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));
            
            // Vérifier que la séance appartient au formateur
            if (!schedule.getCours().getFormateur().getId().equals(trainer.getId())) {
                throw new RuntimeException("Accès non autorisé");
            }
            
            model.addAttribute("schedule", schedule);
            model.addAttribute("course", schedule.getCours());
            return "formateur/schedule-form";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/formateur/schedules";
        }
    }
    
    /**
     * Met à jour une séance
     */
    @PostMapping("/schedules/{scheduleId}")
    public String updateSchedule(@PathVariable Long scheduleId,
                                @ModelAttribute Schedule scheduleDetails,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            Schedule schedule = scheduleService.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));
            
            // Vérifier que la séance appartient au formateur
            if (!schedule.getCours().getFormateur().getId().equals(trainer.getId())) {
                throw new RuntimeException("Accès non autorisé");
            }
            
            // Maintenir le statut APPROVED après modification pour qu'elle reste visible
            scheduleDetails.setStatus(Schedule.ScheduleStatus.APPROVED);
            scheduleService.update(scheduleId, scheduleDetails);
            redirectAttributes.addFlashAttribute("success", 
                "Séance modifiée avec succès. Les étudiants ont été notifiés de la modification.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la modification: " + e.getMessage());
        }
        return "redirect:/formateur/schedules";
    }
    
    /**
     * Supprime une séance
     */
    @PostMapping("/schedules/{scheduleId}/delete")
    public String deleteSchedule(@PathVariable Long scheduleId,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            Trainer trainer = formateurService.getCurrentTrainer(getUsername(authentication));
            Schedule schedule = scheduleService.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));
            
            // Vérifier que la séance appartient au formateur
            if (!schedule.getCours().getFormateur().getId().equals(trainer.getId())) {
                throw new RuntimeException("Accès non autorisé");
            }
            
            scheduleService.delete(scheduleId);
            redirectAttributes.addFlashAttribute("success", "Séance supprimée avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/formateur/schedules";
    }
}

