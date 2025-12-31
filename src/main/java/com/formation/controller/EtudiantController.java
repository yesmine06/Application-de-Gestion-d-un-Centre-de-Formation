package com.formation.controller;

import com.formation.entity.*;
import com.formation.service.EtudiantService;
import com.formation.service.GradeService;
import com.formation.service.ScheduleService;
import com.formation.service.EnrollmentService;
import com.formation.service.CourseFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/etudiant")
@PreAuthorize("hasAnyRole('ETUDIANT', 'ADMIN', 'FORMATEUR')")
public class EtudiantController extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(EtudiantController.class);
    
    private final EtudiantService etudiantService;
    private final GradeService gradeService;
    private final ScheduleService scheduleService;
    private final EnrollmentService enrollmentService;
    private final CourseFileService courseFileService;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public EtudiantController(EtudiantService etudiantService,
                             GradeService gradeService,
                             ScheduleService scheduleService,
                             EnrollmentService enrollmentService,
                             CourseFileService courseFileService,
                             ObjectMapper objectMapper) {
        this.etudiantService = etudiantService;
        this.gradeService = gradeService;
        this.scheduleService = scheduleService;
        this.enrollmentService = enrollmentService;
        this.courseFileService = courseFileService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Affiche les cours disponibles pour s'inscrire
     */
    @GetMapping("/courses/available")
    public String availableCourses(Model model, Authentication authentication) {
        try {
            Student student = etudiantService.getCurrentStudent(getUsername(authentication));
            List<Course> availableCourses = etudiantService.getAvailableCourses(student.getId());
            
            model.addAttribute("courses", availableCourses);
            model.addAttribute("student", student);
            return "etudiant/available-courses";
        } catch (Exception e) {
            return handleException(e, model, "etudiant/dashboard");
        }
    }
    
    /**
     * Inscrit l'étudiant à un cours
     */
    @PostMapping("/courses/{courseId}/enroll")
    public String enrollInCourse(@PathVariable Long courseId,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            Student student = etudiantService.getCurrentStudent(getUsername(authentication));
            enrollmentService.enroll(student.getId(), courseId);
            redirectAttributes.addFlashAttribute("success", "Inscription réussie au cours");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'inscription: " + e.getMessage());
        }
        return "redirect:/etudiant/courses/available";
    }
    
    /**
     * Désinscrit l'étudiant d'un cours
     */
    @PostMapping("/courses/{courseId}/unenroll")
    public String unenrollFromCourse(@PathVariable Long courseId,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        try {
            Student student = etudiantService.getCurrentStudent(getUsername(authentication));
            enrollmentService.unenroll(student.getId(), courseId);
            redirectAttributes.addFlashAttribute("success", "Désinscription réussie");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la désinscription: " + e.getMessage());
        }
        return "redirect:/etudiant/courses";
    }
    
    /**
     * Affiche les cours de l'étudiant
     */
    @GetMapping("/courses")
    public String myCourses(Model model, Authentication authentication) {
        try {
            Student student = etudiantService.getCurrentStudent(getUsername(authentication));
            List<Course> courses = etudiantService.getStudentCourses(student.getId());
            List<Grade> grades = etudiantService.getStudentGrades(student.getId());
            
            // Créer une Map pour faciliter l'accès aux notes par cours
            Map<Long, Grade> gradeMap = grades.stream()
                .collect(Collectors.toMap(
                    g -> g.getCours().getId(),
                    g -> g,
                    (existing, replacement) -> existing
                ));
            
            model.addAttribute("courses", courses);
            model.addAttribute("gradeMap", gradeMap);
            model.addAttribute("student", student);
            return "etudiant/courses";
        } catch (Exception e) {
            return handleException(e, model, "etudiant/dashboard");
        }
    }
    
    /**
     * Affiche les détails d'un cours avec les notes de l'étudiant
     */
    @GetMapping("/courses/{courseId}")
    public String courseDetails(@PathVariable Long courseId, Model model, Authentication authentication) {
        try {
            Student student = etudiantService.getCurrentStudent(getUsername(authentication));
            
            // Vérifier que l'étudiant est inscrit au cours
            if (!etudiantService.isEnrolledInCourse(student.getId(), courseId)) {
                model.addAttribute("error", "Vous n'êtes pas inscrit à ce cours");
                return "redirect:/etudiant/courses";
            }
            
            List<Course> courses = etudiantService.getStudentCourses(student.getId());
            Course course = courses.stream()
                .filter(c -> c.getId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
            
            Optional<Grade> gradeOpt = etudiantService.getGradeByCourse(student.getId(), courseId);
            
            // Récupérer les fichiers du cours
            List<CourseFile> files = courseFileService.getFilesByCourse(courseId);
            
            model.addAttribute("course", course);
            model.addAttribute("grade", gradeOpt.orElse(null));
            model.addAttribute("student", student);
            model.addAttribute("files", files);
            
            return "etudiant/course-details";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/etudiant/courses";
        }
    }
    
    /**
     * Affiche toutes les notes de l'étudiant
     */
    @GetMapping("/grades")
    public String myGrades(Model model, Authentication authentication) {
        try {
            Student student = etudiantService.getCurrentStudent(getUsername(authentication));
            List<Grade> grades = etudiantService.getStudentGrades(student.getId());
            
            // Calculer la moyenne générale
            Double average = GradeService.calculateAverage(grades);
            
            model.addAttribute("grades", grades);
            model.addAttribute("student", student);
            model.addAttribute("average", average);
            return "etudiant/grades";
        } catch (Exception e) {
            return handleException(e, model, "etudiant/dashboard");
        }
    }
    
    /**
     * Affiche l'emploi du temps de l'étudiant
     */
    @GetMapping("/schedule")
    public String mySchedule(@RequestParam(required = false) String date,
                            Model model, Authentication authentication) {
        try {
            Student student = etudiantService.getCurrentStudent(getUsername(authentication));
            LocalDate scheduleDate = date != null ? 
                LocalDate.parse(date) : LocalDate.now();
            
            // Récupérer toutes les séances approuvées pour le calendrier
            List<Schedule> allSchedules = scheduleService.getAllStudentSchedules(student.getId());
            
            // Filtrer par date pour la liste (si une date est spécifiée)
            List<Schedule> schedulesForDate = allSchedules.stream()
                .filter(s -> s.getDate().equals(scheduleDate))
                .toList();
            
            // Créer une liste de maps pour faciliter le traitement JavaScript
            List<Map<String, Object>> schedulesForJS = allSchedules.stream()
                .map(s -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", s.getId());
                    map.put("date", s.getDate().toString()); // Format YYYY-MM-DD
                    map.put("heureDebut", s.getHeureDebut() != null ? s.getHeureDebut().toString() : "09:00:00");
                    map.put("heureFin", s.getHeureFin() != null ? s.getHeureFin().toString() : "11:00:00");
                    map.put("salle", s.getSalle() != null ? s.getSalle() : "-");
                    map.put("status", s.getStatus() != null ? s.getStatus().toString() : "PENDING");
                    // Informations du cours
                    Map<String, Object> coursMap = new java.util.HashMap<>();
                    if (s.getCours() != null) {
                        coursMap.put("id", s.getCours().getId());
                        coursMap.put("titre", s.getCours().getTitre());
                        coursMap.put("code", s.getCours().getCode());
                        // Informations du formateur
                        if (s.getCours().getFormateur() != null) {
                            Map<String, Object> formateurMap = new java.util.HashMap<>();
                            formateurMap.put("id", s.getCours().getFormateur().getId());
                            formateurMap.put("nom", s.getCours().getFormateur().getNom());
                            formateurMap.put("prenom", s.getCours().getFormateur().getPrenom());
                            coursMap.put("formateur", formateurMap);
                        }
                    }
                    map.put("cours", coursMap);
                    return map;
                })
                .toList();
            
            // Convertir en JSON pour JavaScript
            String schedulesJson = objectMapper.writeValueAsString(schedulesForJS);
            
            model.addAttribute("schedules", schedulesForDate);
            model.addAttribute("allSchedules", allSchedules);
            model.addAttribute("schedulesForJS", schedulesForJS);
            model.addAttribute("schedulesJson", schedulesJson); // JSON string pour JavaScript
            model.addAttribute("student", student);
            model.addAttribute("selectedDate", scheduleDate);
            
            return "etudiant/schedule";
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de l'emploi du temps", e);
            model.addAttribute("error", "Erreur: " + e.getMessage());
            return "etudiant/dashboard";
        }
    }
    
    /**
     * Télécharge un fichier d'un cours
     */
    @GetMapping("/courses/{courseId}/files/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long courseId,
                                                 @PathVariable Long fileId,
                                                 Authentication authentication) {
        try {
            Student student = etudiantService.getCurrentStudent(getUsername(authentication));
            
            // Vérifier que l'étudiant est inscrit au cours
            if (!etudiantService.isEnrolledInCourse(student.getId(), courseId)) {
                return ResponseEntity.notFound().build();
            }
            
            CourseFile file = courseFileService.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Fichier non trouvé"));
            
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
}

