package com.formation.repository;

import com.formation.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByStudentIdAndCoursId(Long studentId, Long coursId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId")
    List<Enrollment> findByStudentId(Long studentId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.cours.id = :coursId")
    List<Enrollment> findByCoursId(Long coursId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.cours.id IN :courseIds")
    List<Enrollment> findByCoursIdIn(List<Long> courseIds);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.cours.id = :coursId")
    Long countByCoursId(Long coursId);
    
    boolean existsByStudentIdAndCoursId(Long studentId, Long coursId);
}


