package com.formation.repository;

import com.formation.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCode(String code);
    boolean existsByCode(String code);
    
    @Query("SELECT c FROM Course c WHERE c.formateur.id = :trainerId")
    List<Course> findByFormateurId(Long trainerId);
    
    @Query("SELECT c FROM Course c WHERE c.session.id = :sessionId")
    List<Course> findBySessionId(Long sessionId);
}


