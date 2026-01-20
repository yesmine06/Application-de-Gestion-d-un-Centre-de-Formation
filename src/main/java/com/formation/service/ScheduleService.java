package com.formation.service;

import com.formation.entity.Schedule;
import com.formation.entity.Course;
import com.formation.entity.Student;
import com.formation.entity.Trainer;
import com.formation.entity.Enrollment;
import com.formation.exception.BusinessException;
import com.formation.exception.ResourceNotFoundException;
import com.formation.repository.ScheduleRepository;
import com.formation.repository.CourseRepository;
import com.formation.repository.StudentRepository;
import com.formation.repository.TrainerRepository;
import com.formation.repository.EnrollmentRepository;
import com.formation.service.util.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ScheduleService extends EntityService<Schedule, Long> {
    
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final TrainerRepository trainerRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EmailService emailService;
    
    @Autowired
    public ScheduleService(ScheduleRepository scheduleRepository,
                          CourseRepository courseRepository,
                          StudentRepository studentRepository,
                          TrainerRepository trainerRepository,
                          EnrollmentRepository enrollmentRepository,
                          EmailService emailService) {
        super(scheduleRepository);
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.trainerRepository = trainerRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.emailService = emailService;
    }
    
    private ScheduleRepository getScheduleRepository() {
        return (ScheduleRepository) repository;
    }
    
    public Schedule save(Schedule schedule) {
        loadCourse(schedule);
        setDefaultStatus(schedule);
        validateNoConflicts(schedule);
        
        Schedule savedSchedule = repository.save(schedule);
        
        if (savedSchedule.getStatus() == Schedule.ScheduleStatus.APPROVED && savedSchedule.getCours() != null) {
            sendScheduleNotificationToStudents(savedSchedule);
        }
        
        return savedSchedule;
    }
    
    private void loadCourse(Schedule schedule) {
        if (schedule.getCours() != null && schedule.getCours().getId() != null) {
            Course course = courseRepository.findById(schedule.getCours().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé"));
            schedule.setCours(course);
        }
    }
    
    private void setDefaultStatus(Schedule schedule) {
        if (schedule.getId() == null && schedule.getStatus() == null) {
            schedule.setStatus(Schedule.ScheduleStatus.PENDING);
        }
    }
    
    private void validateNoConflicts(Schedule schedule) {
        if (schedule.getCours() != null && schedule.getCours().getFormateur() != null) {
            List<Schedule> conflicts = findConflictingSchedulesForTrainer(
                schedule.getCours().getFormateur().getId(),
                schedule.getDate(),
                schedule.getHeureDebut(),
                schedule.getHeureFin(),
                schedule.getId()
            );
            if (!conflicts.isEmpty()) {
                throw new BusinessException("Conflit d'horaires pour le formateur: " + conflicts.size() + " séance(s) conflictuelle(s)");
            }
        }
    }
    
    /**
     * Trouve les conflits pour un formateur (en excluant une séance spécifique si édition)
     */
    private List<Schedule> findConflictingSchedulesForTrainer(Long trainerId, LocalDate date, 
                                                              LocalTime heureDebut, LocalTime heureFin, Long excludeScheduleId) {
        List<Schedule> allConflicts = getScheduleRepository().findConflictingSchedulesForTrainer(
            trainerId, date, heureDebut, heureFin
        );
        
        if (excludeScheduleId != null) {
            allConflicts.removeIf(s -> s.getId().equals(excludeScheduleId));
        }
        
        return allConflicts;
    }
    
    /**
     * Envoie une notification email aux étudiants inscrits au cours
     */
    private void sendScheduleNotificationToStudents(Schedule schedule) {
        Course course = schedule.getCours();
        List<Enrollment> enrollments = enrollmentRepository.findByCoursId(course.getId());
        
        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            if (student.getEmail() != null && !student.getEmail().isEmpty()) {
                emailService.sendScheduleNotification(
                    student.getEmail(),
                    student.getNom() + " " + student.getPrenom(),
                    course.getTitre(),
                    schedule.getDate(),
                    schedule.getHeureDebut(),
                    schedule.getHeureFin(),
                    schedule.getSalle()
                );
            }
        }
    }
    
    public Schedule update(Long id, Schedule scheduleDetails) {
        Schedule schedule = findByIdOrThrow(id, "Planning");
        
        // Charger le cours si un nouveau cours est fourni
        if (scheduleDetails.getCours() != null && scheduleDetails.getCours().getId() != null) {
            loadCourse(scheduleDetails); // Charger le cours complet depuis la base de données
        }
        
        updateBasicFields(schedule, scheduleDetails);
        validateNoConflicts(schedule);
        
        Schedule savedSchedule = repository.save(schedule);
        
        if (savedSchedule.getStatus() == Schedule.ScheduleStatus.APPROVED) {
            sendScheduleNotificationToStudents(savedSchedule);
        }
        
        return savedSchedule;
    }
    
    private void updateBasicFields(Schedule schedule, Schedule scheduleDetails) {
        schedule.setDate(scheduleDetails.getDate());
        schedule.setHeureDebut(scheduleDetails.getHeureDebut());
        schedule.setHeureFin(scheduleDetails.getHeureFin());
        schedule.setSalle(scheduleDetails.getSalle());
        
        // Mettre à jour le cours si fourni
        if (scheduleDetails.getCours() != null) {
            schedule.setCours(scheduleDetails.getCours());
        }
        
        if (scheduleDetails.getStatus() != null) {
            schedule.setStatus(scheduleDetails.getStatus());
        }
    }
    
    /**
     * Approuve une séance (changé le statut à APPROVED)
     */
    public Schedule approve(Long id) {
        Schedule schedule = findByIdOrThrow(id, "Planning");
        schedule.setStatus(Schedule.ScheduleStatus.APPROVED);
        
        Schedule savedSchedule = repository.save(schedule);
        sendScheduleNotificationToStudents(savedSchedule);
        
        return savedSchedule;
    }
    
    /**
     * Rejette une séance (changé le statut à REJECTED)
     */
    public Schedule reject(Long id) {
        Schedule schedule = findByIdOrThrow(id, "Planning");
        schedule.setStatus(Schedule.ScheduleStatus.REJECTED);
        return repository.save(schedule);
    }
    
    /**
     * Trouve tous les conflits entre toutes les séances (pour l'admin)
     */
    public List<Schedule> findAllConflicts() {
        return getScheduleRepository().findAllConflictingSchedules();
    }
    
    /**
     * Trouve toutes les séances en attente de validation
     */
    public List<Schedule> findPendingSchedules() {
        return getScheduleRepository().findPendingSchedules();
    }
    
    /**
     * Trouve toutes les séances d'un formateur
     */
    public List<Schedule> findByTrainer(Long trainerId) {
        return getScheduleRepository().findByTrainerId(trainerId);
    }
    
    public List<Schedule> findByCourse(Long coursId) {
        return getScheduleRepository().findByCoursId(coursId);
    }
    
    public void delete(Long id) {
        deleteById(id);
    }
    
    public List<Schedule> getStudentSchedule(Long studentId, LocalDate date) {
        return getScheduleRepository().findStudentSchedule(studentId, date);
    }
    
    /**
     * Récupère toutes les séances approuvées d'un étudiant (pour le calendrier)
     */
    public List<Schedule> getAllStudentSchedules(Long studentId) {
        return getScheduleRepository().findAllStudentSchedules(studentId);
    }
    
    public boolean hasConflictForStudent(Long studentId, LocalDate date, 
                                        LocalTime heureDebut, LocalTime heureFin) {
        List<Schedule> conflicts = getScheduleRepository().findConflictingSchedulesForStudent(
            studentId, date, heureDebut, heureFin
        );
        return !conflicts.isEmpty();
    }
    
    public boolean hasConflictForTrainer(Long trainerId, LocalDate date, 
                                        LocalTime heureDebut, LocalTime heureFin) {
        List<Schedule> conflicts = getScheduleRepository().findConflictingSchedulesForTrainer(
            trainerId, date, heureDebut, heureFin
        );
        return !conflicts.isEmpty();
    }
}



