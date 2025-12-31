package com.formation.repository;

import com.formation.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @Query("SELECT s FROM Schedule s WHERE s.cours.id = :coursId")
    List<Schedule> findByCoursId(Long coursId);
    
    @Query("SELECT s FROM Schedule s WHERE s.date = :date AND s.status = 'APPROVED' AND s.cours.id IN " +
           "(SELECT e.cours.id FROM Enrollment e WHERE e.student.id = :studentId)")
    List<Schedule> findStudentSchedule(Long studentId, LocalDate date);
    
    /**
     * Trouve toutes les séances approuvées d'un étudiant (sans filtre de date)
     */
    @Query("SELECT s FROM Schedule s WHERE s.status = 'APPROVED' AND s.cours.id IN " +
           "(SELECT e.cours.id FROM Enrollment e WHERE e.student.id = :studentId) " +
           "ORDER BY s.date, s.heureDebut")
    List<Schedule> findAllStudentSchedules(Long studentId);
    
    @Query("SELECT s FROM Schedule s WHERE s.date = :date AND s.heureDebut <= :heureFin " +
           "AND s.heureFin >= :heureDebut AND s.cours.formateur.id = :trainerId")
    List<Schedule> findConflictingSchedulesForTrainer(Long trainerId, LocalDate date, 
                                                       LocalTime heureDebut, LocalTime heureFin);
    
    @Query("SELECT s FROM Schedule s WHERE s.date = :date AND s.heureDebut <= :heureFin " +
           "AND s.heureFin >= :heureDebut AND s.cours.id IN " +
           "(SELECT e.cours.id FROM Enrollment e WHERE e.student.id = :studentId)")
    List<Schedule> findConflictingSchedulesForStudent(Long studentId, LocalDate date, 
                                                       LocalTime heureDebut, LocalTime heureFin);
    
    /**
     * Trouve toutes les séances qui ont des conflits entre formateurs
     * (deux formateurs différents ont des séances au même moment)
     */
    @Query("SELECT s1 FROM Schedule s1, Schedule s2 " +
           "WHERE s1.id != s2.id " +
           "AND s1.date = s2.date " +
           "AND s1.heureDebut < s2.heureFin " +
           "AND s1.heureFin > s2.heureDebut " +
           "AND s1.cours.formateur.id != s2.cours.formateur.id " +
           "AND s1.status = 'APPROVED' " +
           "AND s2.status = 'APPROVED'")
    List<Schedule> findAllConflictingSchedules();
    
    /**
     * Trouve toutes les séances en attente de validation
     */
    @Query("SELECT s FROM Schedule s WHERE s.status = 'PENDING' ORDER BY s.date, s.heureDebut")
    List<Schedule> findPendingSchedules();
    
    /**
     * Trouve toutes les séances d'un formateur
     */
    @Query("SELECT s FROM Schedule s WHERE s.cours.formateur.id = :trainerId ORDER BY s.date, s.heureDebut")
    List<Schedule> findByTrainerId(Long trainerId);
}


