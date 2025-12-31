package com.formation.service;

import com.formation.constants.AppConstants;
import com.formation.entity.Course;
import com.formation.entity.Student;
import com.formation.entity.Trainer;
import com.formation.entity.Group;
import com.formation.entity.Specialty;
import com.formation.entity.Session;
import com.formation.repository.CourseRepository;
import com.formation.repository.StudentRepository;
import com.formation.repository.TrainerRepository;
import com.formation.repository.GradeRepository;
import com.formation.repository.EnrollmentRepository;
import com.formation.repository.GroupRepository;
import com.formation.repository.SpecialtyRepository;
import com.formation.repository.SessionRepository;
import com.formation.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class StatisticsService {
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private TrainerRepository trainerRepository;
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private SpecialtyRepository specialtyRepository;
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private GradeService gradeService;
    
    /**
     * Récupère toutes les statistiques pour le dashboard admin
     * Cache pendant 5 minutes car les statistiques changent peu fréquemment
     */
    @org.springframework.cache.annotation.Cacheable(value = "statistics", key = "'admin'", unless = "#result == null")
    public Map<String, Object> getAdminStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        addBasicStatistics(stats);
        addTrainerStatistics(stats);
        addGroupStatistics(stats);
        addSpecialtyStatistics(stats);
        addGradeStatistics(stats);
        addCourseStatistics(stats);
        addSessionStatistics(stats);
        addActivityStatistics(stats);
        
        return stats;
    }
    
    private void addBasicStatistics(Map<String, Object> stats) {
        stats.put("totalStudents", studentRepository.count());
        stats.put("totalTrainers", trainerRepository.count());
        stats.put("totalCourses", courseRepository.count());
        stats.put("totalEnrollments", enrollmentRepository.count());
        stats.put("totalGrades", gradeRepository.count());
        stats.put("totalGroups", groupRepository.count());
        stats.put("totalSpecialties", specialtyRepository.count());
        stats.put("totalSessions", sessionRepository.count());
        stats.put("totalSchedules", scheduleRepository.count());
    }
    
    private void addTrainerStatistics(Map<String, Object> stats) {
        List<Trainer> allTrainers = trainerRepository.findAll();
        stats.put("trainersWithCourses", allTrainers.stream()
            .filter(t -> t.getCourses() != null && !t.getCourses().isEmpty())
            .count());
        stats.put("trainersList", allTrainers.stream()
            .map(this::mapTrainerToInfo)
            .collect(Collectors.toList()));
    }
    
    private Map<String, Object> mapTrainerToInfo(Trainer trainer) {
        Map<String, Object> trainerInfo = new HashMap<>();
        trainerInfo.put("id", trainer.getId());
        trainerInfo.put("nom", trainer.getNom());
        trainerInfo.put("prenom", trainer.getPrenom());
        trainerInfo.put("specialite", trainer.getSpecialite());
        trainerInfo.put("email", trainer.getEmail());
        trainerInfo.put("coursesCount", trainer.getCourses() != null ? trainer.getCourses().size() : 0);
        return trainerInfo;
    }
    
    private void addGroupStatistics(Map<String, Object> stats) {
        List<Group> allGroups = groupRepository.findAll();
        stats.put("groupsWithStudents", allGroups.stream()
            .filter(g -> g.getStudents() != null && !g.getStudents().isEmpty())
            .count());
        stats.put("groupsStatistics", allGroups.stream()
            .map(this::mapGroupToInfo)
            .sorted((a, b) -> Integer.compare(
                (Integer) b.get("studentsCount"),
                (Integer) a.get("studentsCount")
            ))
            .collect(Collectors.toList()));
    }
    
    private Map<String, Object> mapGroupToInfo(Group group) {
        Map<String, Object> groupInfo = new HashMap<>();
        groupInfo.put("id", group.getId());
        groupInfo.put("nom", group.getNom());
        groupInfo.put("description", group.getDescription());
        groupInfo.put("studentsCount", group.getStudents() != null ? group.getStudents().size() : 0);
        groupInfo.put("coursesCount", group.getCourses() != null ? group.getCourses().size() : 0);
        return groupInfo;
    }
    
    private void addSpecialtyStatistics(Map<String, Object> stats) {
        List<Specialty> allSpecialties = specialtyRepository.findAll();
        stats.put("specialtiesStatistics", allSpecialties.stream()
            .map(this::mapSpecialtyToInfo)
            .sorted((a, b) -> Integer.compare(
                (Integer) b.get("studentsCount"),
                (Integer) a.get("studentsCount")
            ))
            .collect(Collectors.toList()));
    }
    
    private Map<String, Object> mapSpecialtyToInfo(Specialty specialty) {
        Map<String, Object> specialtyInfo = new HashMap<>();
        specialtyInfo.put("id", specialty.getId());
        specialtyInfo.put("nom", specialty.getNom());
        specialtyInfo.put("description", specialty.getDescription());
        specialtyInfo.put("studentsCount", specialty.getStudents() != null ? specialty.getStudents().size() : 0);
        return specialtyInfo;
    }
    
    private void addGradeStatistics(Map<String, Object> stats) {
        List<com.formation.entity.Grade> allGrades = gradeRepository.findAll();
        double averageGrade = allGrades.stream()
            .mapToDouble(g -> g.getValeur() != null ? g.getValeur() : 0.0)
            .average()
            .orElse(0.0);
        stats.put("averageGrade", averageGrade);
        stats.put("gradesAbove10", allGrades.stream()
            .filter(g -> g.getValeur() != null && g.getValeur() >= AppConstants.PASSING_GRADE)
            .count());
        stats.put("gradesBelow10", allGrades.stream()
            .filter(g -> g.getValeur() != null && g.getValeur() < AppConstants.PASSING_GRADE)
            .count());
    }
    
    private void addCourseStatistics(Map<String, Object> stats) {
        List<Course> allCourses = courseRepository.findAll();
        stats.put("coursesWithEnrollments", allCourses.stream()
            .filter(c -> enrollmentRepository.countByCoursId(c.getId()) > 0)
            .count());
        
        Map<String, Long> courseEnrollments = new HashMap<>();
        Map<String, Double> courseSuccessRates = new HashMap<>();
        Map<String, Double> courseAverages = new HashMap<>();
        
        for (Course course : allCourses) {
            Long enrollmentCount = enrollmentRepository.countByCoursId(course.getId());
            courseEnrollments.put(course.getTitre(), enrollmentCount);
            courseSuccessRates.put(course.getTitre(), gradeService.calculateCourseSuccessRate(course.getId()));
            courseAverages.put(course.getTitre(), gradeService.calculateCourseAverage(course.getId()));
        }
        
        stats.put("courseEnrollments", courseEnrollments);
        stats.put("courseSuccessRates", courseSuccessRates);
        stats.put("courseAverages", courseAverages);
    }
    
    private void addSessionStatistics(Map<String, Object> stats) {
        List<Session> allSessions = sessionRepository.findAll();
        stats.put("sessionsStatistics", allSessions.stream()
            .map(this::mapSessionToInfo)
            .collect(Collectors.toList()));
    }
    
    private Map<String, Object> mapSessionToInfo(Session session) {
        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("id", session.getId());
        sessionInfo.put("nom", session.getNom());
        sessionInfo.put("dateDebut", session.getDateDebut());
        sessionInfo.put("dateFin", session.getDateFin());
        sessionInfo.put("type", session.getType());
        return sessionInfo;
    }
    
    private void addActivityStatistics(Map<String, Object> stats) {
        stats.put("totalActiveEnrollments", enrollmentRepository.count());
        stats.put("studentsWithGrades", gradeRepository.findAll().stream()
            .map(g -> g.getStudent().getId())
            .distinct()
            .count());
    }
    
    /**
     * Récupère les cours les plus suivis
     * Cache pendant 10 minutes
     */
    @Cacheable(value = "statistics", key = "'mostFollowedCourses:' + #limit")
    public List<Map<String, Object>> getMostFollowedCourses(int limit) {
        List<Course> allCourses = courseRepository.findAll();
        return allCourses.stream()
            .map(course -> {
                Map<String, Object> courseData = new HashMap<>();
                courseData.put("course", course);
                courseData.put("enrollmentCount", enrollmentRepository.countByCoursId(course.getId()));
                courseData.put("successRate", gradeService.calculateCourseSuccessRate(course.getId()));
                courseData.put("courseAverage", gradeService.calculateCourseAverage(course.getId()));
                return courseData;
            })
            .sorted((a, b) -> Long.compare(
                (Long) b.get("enrollmentCount"),
                (Long) a.get("enrollmentCount")
            ))
            .limit(limit)
            .toList();
    }
}

