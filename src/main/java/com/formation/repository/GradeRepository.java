package com.formation.repository;

import com.formation.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    @Query("SELECT g FROM Grade g JOIN FETCH g.student JOIN FETCH g.cours WHERE g.student.id = :studentId AND g.cours.id = :coursId")
    Optional<Grade> findByStudentIdAndCoursId(Long studentId, Long coursId);
    
    @Query("SELECT g FROM Grade g JOIN FETCH g.student JOIN FETCH g.cours WHERE g.student.id = :studentId")
    List<Grade> findByStudentId(Long studentId);
    
    @Query("SELECT g FROM Grade g JOIN FETCH g.student JOIN FETCH g.cours WHERE g.cours.id = :coursId")
    List<Grade> findByCoursId(Long coursId);
    
    @Query("SELECT g FROM Grade g WHERE g.cours.id IN :courseIds")
    List<Grade> findByCoursIdIn(List<Long> courseIds);
    
    @Query("SELECT AVG(g.valeur) FROM Grade g WHERE g.student.id = :studentId")
    Double calculateAverageByStudentId(Long studentId);
    
    @Query("SELECT COUNT(g) FROM Grade g WHERE g.cours.id = :coursId AND g.valeur >= 10")
    Long countPassingGradesByCoursId(Long coursId);
    
    @Query("SELECT COUNT(g) FROM Grade g WHERE g.cours.id = :coursId")
    Long countTotalGradesByCoursId(Long coursId);
}


